package com.zhubao.manage.module.shop.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhubao.manage.common.dto.PageDTO;
import com.zhubao.manage.common.dto.PageResult;
import com.zhubao.manage.common.exception.BusinessException;
import com.zhubao.manage.module.marketing.entity.Promotion;
import com.zhubao.manage.module.marketing.mapper.PromotionMapper;
import com.zhubao.manage.module.shop.entity.UserCoupon;
import com.zhubao.manage.module.shop.mapper.UserCouponMapper;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.LinkedHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * C端优惠券服务 —— 营销三端打通：
 * 管理端配置促销(promotion) → C端用户领取(user_coupon) → 下单抵扣(coupon_discount)
 */
@Service
public class ShopCouponService {

    private final UserCouponMapper couponMapper;
    private final PromotionMapper promotionMapper;

    public ShopCouponService(UserCouponMapper cm, PromotionMapper pm) {
        this.couponMapper = cm;
        this.promotionMapper = pm;
    }

    // ==================== 可领取列表 ====================

    /**
     * 当前可领取的促销（ongoing 且在有效期），标注用户是否已领过
     */
    public List<Map<String, Object>> availableCoupons(Long userId) {
        String today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        List<Promotion> promos = promotionMapper.selectList(new LambdaQueryWrapper<Promotion>()
                .eq(Promotion::getStatus, "ongoing")
                .and(w -> w.le(Promotion::getStartTime, today).or().isNull(Promotion::getStartTime))
                .and(w -> w.ge(Promotion::getEndTime, today).or().isNull(Promotion::getEndTime))
                .orderByDesc(Promotion::getCreatedAt));

        // 已领取的 promotionId 集合（含已使用/未使用，避免重复领取）
        List<Long> claimed = couponMapper.selectList(new LambdaQueryWrapper<UserCoupon>()
                        .eq(UserCoupon::getUserId, userId))
                .stream().map(UserCoupon::getPromotionId).collect(Collectors.toList());

        return promos.stream().map(p -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("promotionId", p.getId());
            m.put("name", p.getName());
            m.put("type", p.getType());
            m.put("discountMethod", p.getDiscountMethod());
            m.put("startTime", p.getStartTime());
            m.put("endTime", p.getEndTime());
            m.put("scope", p.getScope());
            m.put("claimed", claimed.contains(p.getId()));
            return m;
        }).collect(Collectors.toList());
    }

    // ==================== 领取 ====================

    @Transactional
    public UserCoupon receive(Long userId, Long promotionId) {
        Promotion p = promotionMapper.selectById(promotionId);
        if (p == null) throw new BusinessException(400, "活动不存在");
        if (!"ongoing".equals(p.getStatus())) throw new BusinessException(400, "活动已结束");

        long exists = couponMapper.selectCount(new LambdaQueryWrapper<UserCoupon>()
                .eq(UserCoupon::getUserId, userId)
                .eq(UserCoupon::getPromotionId, promotionId));
        if (exists > 0) throw new BusinessException(400, "该优惠券已领取，请勿重复领取");

        // 解析优惠文案 → 门槛/优惠值
        ParsedCoupon parsed = parseDiscountMethod(p.getType(), p.getDiscountMethod());

        UserCoupon c = new UserCoupon();
        c.setUserId(userId);
        c.setPromotionId(p.getId());
        c.setName(p.getName());
        c.setType(p.getType());
        c.setDiscountMethod(p.getDiscountMethod());
        c.setThreshold(parsed.threshold);
        c.setDiscountValue(parsed.discountValue);
        c.setStatus("UNUSED");
        c.setExpireTime(parseExpire(p.getEndTime()));
        couponMapper.insert(c);

        // 领取次数 +1
        p.setUsageCount((p.getUsageCount() == null ? 0 : p.getUsageCount()) + 1);
        promotionMapper.updateById(p);
        return c;
    }

    // ==================== 我的优惠券 ====================

    public IPage<UserCoupon> myCoupons(Long userId, PageDTO dto, String status) {
        LambdaQueryWrapper<UserCoupon> w = new LambdaQueryWrapper<>();
        w.eq(UserCoupon::getUserId, userId);
        if (StringUtils.isNotBlank(status)) w.eq(UserCoupon::getStatus, status);
        w.orderByDesc(UserCoupon::getReceivedAt);
        return couponMapper.selectPage(new Page<>(dto.getPageNum(), dto.getPageSize()), w);
    }

    public long countUnused(Long userId) {
        return couponMapper.selectCount(new LambdaQueryWrapper<UserCoupon>()
                .eq(UserCoupon::getUserId, userId)
                .eq(UserCoupon::getStatus, "UNUSED"));
    }

    // ==================== 下单应用 ====================

    /**
     * 校验并计算优惠券可抵扣金额。
     * @return 抵扣金额（0 表示不可用）
     */
    public BigDecimal calcDiscount(Long userId, Long couponId, BigDecimal orderAmount) {
        if (couponId == null || orderAmount == null) return BigDecimal.ZERO;
        UserCoupon c = couponMapper.selectById(couponId);
        if (c == null) throw new BusinessException(400, "优惠券不存在");
        if (!c.getUserId().equals(userId)) throw new BusinessException(400, "优惠券不属于当前用户");
        if (!"UNUSED".equals(c.getStatus())) throw new BusinessException(400, "优惠券不可用");
        if (c.getExpireTime() != null && c.getExpireTime().isBefore(LocalDateTime.now())) {
            c.setStatus("EXPIRED");
            couponMapper.updateById(c);
            throw new BusinessException(400, "优惠券已过期");
        }
        // 门槛校验
        if (c.getThreshold() != null && c.getThreshold().compareTo(BigDecimal.ZERO) > 0
                && orderAmount.compareTo(c.getThreshold()) < 0) {
            throw new BusinessException(400, "订单金额未达优惠券使用门槛");
        }
        BigDecimal discount;
        if ("discount".equals(c.getType())) {
            // 折扣券：discountValue=8.5 表示 85 折 → 优惠 15%
            BigDecimal rate = c.getDiscountValue() != null ? c.getDiscountValue() : BigDecimal.TEN;
            if (rate.compareTo(BigDecimal.ZERO) <= 0 || rate.compareTo(BigDecimal.TEN) > 0) rate = BigDecimal.TEN;
            discount = orderAmount.multiply(BigDecimal.TEN.subtract(rate)).divide(BigDecimal.TEN, 2, BigDecimal.ROUND_HALF_UP);
        } else {
            // 满减券：discountValue = 减免金额
            discount = c.getDiscountValue() != null ? c.getDiscountValue() : BigDecimal.ZERO;
        }
        // 抵扣不超过订单金额
        if (discount.compareTo(orderAmount) > 0) discount = orderAmount;
        return discount;
    }

    /**
     * 下单成功后标记优惠券已使用
     */
    @Transactional
    public void markUsed(Long userId, Long couponId, Long orderId, BigDecimal discount) {
        if (couponId == null) return;
        UserCoupon c = couponMapper.selectById(couponId);
        if (c == null) return;
        c.setStatus("USED");
        c.setUsedOrderId(orderId);
        c.setUsedAt(LocalDateTime.now());
        couponMapper.updateById(c);
    }

    // ==================== 内部工具 ====================

    private static class ParsedCoupon {
        BigDecimal threshold = BigDecimal.ZERO;
        BigDecimal discountValue = BigDecimal.ZERO;
    }

    /** 解析 "满1000减100" / "钻石会员8折" 等文案 */
    static ParsedCoupon parseDiscountMethod(String type, String text) {
        ParsedCoupon r = new ParsedCoupon();
        if (text == null) return r;
        // 满X：门槛
        Matcher m1 = Pattern.compile("满\\s*(\\d+(?:\\.\\d+)?)").matcher(text);
        if (m1.find()) r.threshold = new BigDecimal(m1.group(1));
        // 减X：满减金额
        Matcher m2 = Pattern.compile("减\\s*(\\d+(?:\\.\\d+)?)").matcher(text);
        if (m2.find()) r.discountValue = new BigDecimal(m2.group(1));
        // X折：折扣（如 8折 / 8.5折）
        if (r.discountValue.compareTo(BigDecimal.ZERO) == 0) {
            Matcher m3 = Pattern.compile("(\\d+(?:\\.\\d+)?)\\s*折").matcher(text);
            if (m3.find()) r.discountValue = new BigDecimal(m3.group(1));
        }
        // 兜底：type=discount 但没解析到折数 → 默认 9 折
        if ("discount".equals(type) && r.discountValue.compareTo(BigDecimal.ZERO) == 0) {
            r.discountValue = new BigDecimal("9");
        }
        return r;
    }

    /** 结束时间转过期时间（当天 23:59:59） */
    static LocalDateTime parseExpire(String endTime) {
        if (StringUtils.isBlank(endTime)) return null;
        try {
            LocalDate d = LocalDate.parse(endTime.trim().substring(0, 10));
            return d.atTime(23, 59, 59);
        } catch (Exception e) {
            return null;
        }
    }
}
