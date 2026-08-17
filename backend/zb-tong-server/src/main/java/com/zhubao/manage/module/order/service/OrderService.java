package com.zhubao.manage.module.order.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhubao.manage.common.interceptor.UserContextHolder;
import com.zhubao.manage.module.order.dto.*;
import com.zhubao.manage.module.order.entity.*;
import com.zhubao.manage.module.order.mapper.*;
import com.zhubao.manage.module.product.entity.Product;
import com.zhubao.manage.module.product.mapper.ProductMapper;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class OrderService {

    private final OrderMapper orderMapper;
    private final OrderItemMapper itemMapper;
    private final OrderLogMapper logMapper;
    private final OrderReturnMapper returnMapper;
    private final UserContextHolder userContextHolder;
    private final ProductMapper productMapper;

    public OrderService(OrderMapper om, OrderItemMapper im, OrderLogMapper lm, OrderReturnMapper rm,
                        UserContextHolder uch, ProductMapper pm) {
        this.orderMapper = om; this.itemMapper = im; this.logMapper = lm; this.returnMapper = rm;
        this.userContextHolder = uch; this.productMapper = pm;
    }

    // ==================== 订单 ====================

    /** 创建订单 — 记录 userId + 创建 OrderItem + 扣库存 */
    @Transactional
    public Order create(Order order) {
        // 记录创建者
        Long userId = userContextHolder.getUserId();
        if (userId != null) order.setUserId(userId);

        if (order.getOrderCode() == null || order.getOrderCode().trim().isEmpty()) {
            order.setOrderCode("OD" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")) +
                    String.format("%03d", (int) (Math.random() * 1000)));
        }
        if (order.getOrderStatus() == null) order.setOrderStatus("PENDING");
        if (order.getPaymentStatus() == null) order.setPaymentStatus("UNPAID");
        if (order.getTotalAmount() == null) order.setTotalAmount(BigDecimal.ZERO);
        if (order.getOrderAmount() == null) order.setOrderAmount(order.getTotalAmount());
        if (order.getFreight() == null) order.setFreight(BigDecimal.ZERO);
        if (order.getCouponDiscount() == null) order.setCouponDiscount(BigDecimal.ZERO);
        orderMapper.insert(order);

        // 创建订单明细 + 扣减库存
        if (order.getItems() != null && !order.getItems().isEmpty()) {
            for (Map<String, Object> it : order.getItems()) {
                OrderItem item = new OrderItem();
                item.setOrderId(order.getId());
                item.setProductCode(getString(it, "productCode"));
                item.setProductName(getString(it, "productName"));
                item.setImageUrl(getString(it, "imageUrl"));
                item.setSpec(getString(it, "spec"));
                item.setQuantity(getInt(it, "quantity", 1));
                item.setPrice(getBigDecimal(it, "price"));
                itemMapper.insert(item);

                // 扣减库存
                Object productIdObj = it.get("productId");
                if (productIdObj != null) {
                    Long productId = productIdObj instanceof Number ? ((Number) productIdObj).longValue() : Long.valueOf(productIdObj.toString());
                    item.setProductId(productId);
                    Product product = productMapper.selectById(productId);
                    if (product != null) {
                        int qty = item.getQuantity() != null ? item.getQuantity() : 1;
                        int curStock = product.getStock() != null ? product.getStock() : 0;
                        product.setStock(Math.max(0, curStock - qty));
                        productMapper.updateById(product);
                    }
                }
            }
        }

        addLog(order.getId(), "创建订单");
        return order;
    }

    /** 分页列表 — CUSTOMER 只看自己订单，其他角色看全部 */
    public IPage<OrderVO> pageOrders(OrderQueryDTO q) {
        LambdaQueryWrapper<Order> w = new LambdaQueryWrapper<>();

        // 顾客角色：只看自己的订单
        Long userId = userContextHolder.getUserId();
        List<String> roles = resolveUserRoles();
        if (roles.contains("ROLE_CUSTOMER") && userId != null) {
            w.eq(Order::getUserId, userId);
        }

        if (StringUtils.isNotBlank(q.getKeyword()))
            w.and(x -> x.like(Order::getOrderCode, q.getKeyword()).or().like(Order::getCustomerName, q.getKeyword()));
        if (StringUtils.isNotBlank(q.getStatus())) w.eq(Order::getOrderStatus, q.getStatus());
        if (StringUtils.isNotBlank(q.getStartDate())) w.ge(Order::getCreatedAt, q.getStartDate() + " 00:00:00");
        if (StringUtils.isNotBlank(q.getEndDate())) w.le(Order::getCreatedAt, q.getEndDate() + " 23:59:59");
        w.orderByDesc(Order::getCreatedAt);

        Page<Order> page = new Page<>(q.getPageNum(), q.getPageSize());
        IPage<Order> result = orderMapper.selectPage(page, w);

        List<Long> orderIds = result.getRecords().stream().map(Order::getId).collect(Collectors.toList());
        Map<Long, List<OrderItem>> itemMap = buildItemMap(orderIds);
        Map<Long, List<OrderLog>> logMap = buildLogMap(orderIds);

        IPage<OrderVO> voPage = new Page<>(q.getPageNum(), q.getPageSize(), result.getTotal());
        voPage.setRecords(result.getRecords().stream().map(o -> toVO(o, itemMap, logMap)).collect(Collectors.toList()));
        return voPage;
    }

    /** 订单详情 — 含 items + logs */
    public OrderVO detail(Long id) {
        Order o = orderMapper.selectById(id);
        if (o == null) return null;
        List<OrderItem> items = itemMapper.selectList(new LambdaQueryWrapper<OrderItem>().eq(OrderItem::getOrderId, id));
        List<OrderLog> logs = logMapper.selectList(new LambdaQueryWrapper<OrderLog>().eq(OrderLog::getOrderId, id));
        Map<Long, List<OrderItem>> im = Collections.singletonMap(id, items);
        Map<Long, List<OrderLog>> lm = Collections.singletonMap(id, logs);
        return toVO(o, im, lm);
    }

    /** 更新订单 */
    @Transactional
    public void update(Long id, Order order) {
        order.setId(id);
        orderMapper.updateById(order);
    }

    /** 取消订单 */
    @Transactional
    public void cancel(Long id) {
        Order o = orderMapper.selectById(id);
        if (o == null || "CANCELLED".equals(o.getOrderStatus())) return;
        o.setOrderStatus("CANCELLED");
        orderMapper.updateById(o);
        OrderLog log = new OrderLog();
        log.setOrderId(id);
        log.setTime(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        log.setContent("订单已取消");
        logMapper.insert(log);
    }

    /** 发货 — 将订单状态从 PAID 更新为 SHIPPED，记录物流信息 */
    @Transactional
    public void ship(Long id, String deliveryCompany, String deliveryTrackNo) {
        Order o = orderMapper.selectById(id);
        if (o == null) throw new RuntimeException("订单不存在");
        String status = o.getOrderStatus() != null ? o.getOrderStatus().toUpperCase() : "";
        if (!"PAID".equals(status)) throw new RuntimeException("当前订单状态不允许发货，仅待发货订单可操作");
        o.setOrderStatus("SHIPPED");
        o.setDeliveryCompany(deliveryCompany);
        o.setDeliveryTrackNo(deliveryTrackNo);
        o.setDeliveryTime(LocalDateTime.now());
        orderMapper.updateById(o);
        addLog(id, "订单已发货，物流公司：" + (deliveryCompany != null ? deliveryCompany : "无") +
                "，运单号：" + (deliveryTrackNo != null ? deliveryTrackNo : "无"));
    }

    /** 删除订单（逻辑删除） */
    public void delete(Long id) { orderMapper.deleteById(id); }

    /** 追加操作日志 */
    @Transactional
    public void addLog(Long orderId, String content) {
        OrderLog log = new OrderLog();
        log.setOrderId(orderId);
        log.setTime(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        log.setContent(content);
        logMapper.insert(log);
    }

    // ==================== 退换货 ====================

    public IPage<OrderReturn> pageReturns(ReturnQueryDTO q) {
        LambdaQueryWrapper<OrderReturn> w = new LambdaQueryWrapper<>();
        if (StringUtils.isNotBlank(q.getKeyword()))
            w.and(x -> x.like(OrderReturn::getReturnCode, q.getKeyword()).or().like(OrderReturn::getOrderCode, q.getKeyword()));
        if (StringUtils.isNotBlank(q.getReturnType())) w.eq(OrderReturn::getReturnType, q.getReturnType());
        if (StringUtils.isNotBlank(q.getStatus()))
            w.apply("LOWER(status) = {0}", q.getStatus().toLowerCase());
        if (StringUtils.isNotBlank(q.getStartDate())) w.ge(OrderReturn::getCreatedAt, q.getStartDate() + " 00:00:00");
        if (StringUtils.isNotBlank(q.getEndDate())) w.le(OrderReturn::getCreatedAt, q.getEndDate() + " 23:59:59");
        w.orderByDesc(OrderReturn::getCreatedAt);
        IPage<OrderReturn> page = returnMapper.selectPage(new Page<>(q.getPageNum(), q.getPageSize()), w);

        // 补全缺失的商品图片和规格信息（历史数据回填）
        List<Long> orderIds = page.getRecords().stream()
                .map(OrderReturn::getOrderId)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
        if (!orderIds.isEmpty()) {
            List<OrderItem> items = itemMapper.selectList(
                    new LambdaQueryWrapper<OrderItem>().in(OrderItem::getOrderId, orderIds));
            Map<Long, OrderItem> firstItemMap = items.stream()
                    .collect(Collectors.toMap(OrderItem::getOrderId, i -> i, (a, b) -> a));
            for (OrderReturn r : page.getRecords()) {
                if (StringUtils.isBlank(r.getImageUrl()) || StringUtils.isBlank(r.getProductSpec())) {
                    OrderItem item = firstItemMap.get(r.getOrderId());
                    if (item != null) {
                        if (StringUtils.isBlank(r.getImageUrl())) r.setImageUrl(item.getImageUrl());
                        if (StringUtils.isBlank(r.getProductSpec())) r.setProductSpec(item.getSpec());
                    }
                }
            }
        }
        return page;
    }

    public OrderReturn detailReturn(Long id) {
        return returnMapper.selectById(id);
    }

    @Transactional
    public void cancelReturn(Long id) {
        OrderReturn r = returnMapper.selectById(id);
        if (r != null && "APPLYING".equalsIgnoreCase(r.getStatus())) {
            r.setStatus("CANCELLED"); returnMapper.updateById(r);
        }
    }

    /** 审核退换货申请 — 将状态设为 REVIEWING */
    @Transactional
    public void reviewReturn(Long id) {
        OrderReturn r = returnMapper.selectById(id);
        if (r == null) throw new RuntimeException("退换货申请不存在");
        String status = r.getStatus() != null ? r.getStatus().toUpperCase() : "";
        if (!"APPLYING".equals(status)) throw new RuntimeException("仅申请中状态可审核");
        r.setStatus("REVIEWING");
        returnMapper.updateById(r);
        addLog(r.getOrderId(), "退换货申请审核中，申请单号：" + r.getReturnCode());
    }

    /** 同意退款/退货 — REVIEWING/APPLYING → APPROVED，同时将订单状态改为 refund */
    @Transactional
    public void approveReturn(Long id) {
        OrderReturn r = returnMapper.selectById(id);
        if (r == null) throw new RuntimeException("退换货申请不存在");
        String status = r.getStatus() != null ? r.getStatus().toUpperCase() : "";
        if (!"APPLYING".equals(status) && !"REVIEWING".equals(status))
            throw new RuntimeException("当前状态不允许同意，仅申请中/审核中可操作");
        r.setStatus("APPROVED");
        returnMapper.updateById(r);

        // 同步订单状态为退款/售后
        Order order = orderMapper.selectById(r.getOrderId());
        if (order != null) {
            order.setOrderStatus("refund");
            orderMapper.updateById(order);
        }
        addLog(r.getOrderId(), "退换货申请已同意，申请单号：" + r.getReturnCode());
    }

    /** 拒绝退款/退货 — REVIEWING/APPLYING → REJECTED */
    @Transactional
    public void rejectReturn(Long id) {
        OrderReturn r = returnMapper.selectById(id);
        if (r == null) throw new RuntimeException("退换货申请不存在");
        String status = r.getStatus() != null ? r.getStatus().toUpperCase() : "";
        if (!"APPLYING".equals(status) && !"REVIEWING".equals(status))
            throw new RuntimeException("当前状态不允许拒绝，仅申请中/审核中可操作");
        r.setStatus("REJECTED");
        returnMapper.updateById(r);
        addLog(r.getOrderId(), "退换货申请已拒绝，申请单号：" + r.getReturnCode());
    }

    /** 确认退款完成 — APPROVED → COMPLETED，恢复库存，更新支付状态 */
    @Transactional
    public void completeReturn(Long id) {
        OrderReturn r = returnMapper.selectById(id);
        if (r == null) throw new RuntimeException("退换货申请不存在");
        String status = r.getStatus() != null ? r.getStatus().toUpperCase() : "";
        if (!"APPROVED".equals(status)) throw new RuntimeException("仅已同意状态可确认完成");
        r.setStatus("COMPLETED");
        returnMapper.updateById(r);

        // 恢复库存
        Order order = orderMapper.selectById(r.getOrderId());
        if (order != null) {
            // 更新支付状态为已退款
            order.setPaymentStatus("REFUNDED");
            order.setOrderStatus("completed");
            orderMapper.updateById(order);

            // 恢复商品库存
            List<OrderItem> items = itemMapper.selectList(
                    new LambdaQueryWrapper<OrderItem>().eq(OrderItem::getOrderId, r.getOrderId()));
            for (OrderItem item : items) {
                if (item.getProductId() != null) {
                    Product product = productMapper.selectById(item.getProductId());
                    if (product != null) {
                        int qty = item.getQuantity() != null ? item.getQuantity() : 0;
                        int curStock = product.getStock() != null ? product.getStock() : 0;
                        product.setStock(curStock + qty);
                        productMapper.updateById(product);
                    }
                }
            }
        }
        addLog(r.getOrderId(), "退款已完成，退款金额：¥" + (r.getRefundAmount() != null ? r.getRefundAmount().toPlainString() : "0") + "，申请单号：" + r.getReturnCode());
    }

    /** 创建退换货申请 — 使用 orderId + refundAmount */
    @Transactional
    public OrderReturn createReturn(OrderReturn r) {
        if (r.getReturnCode() == null || r.getReturnCode().trim().isEmpty()) {
            r.setReturnCode("RT" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")) +
                    String.format("%03d", (int) (Math.random() * 1000)));
        }
        if (r.getStatus() == null) r.setStatus("APPLYING");
        if (r.getApplyTime() == null) r.setApplyTime(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        // 退款金额默认取订单金额
        if (r.getRefundAmount() == null) r.setRefundAmount(r.getOrderAmount());
        returnMapper.insert(r);
        return r;
    }

    /** 更新退换货申请 */
    @Transactional
    public void updateReturn(Long id, OrderReturn r) { r.setId(id); returnMapper.updateById(r); }

    /** 删除退换货申请 */
    public void deleteReturn(Long id) { returnMapper.deleteById(id); }

    // ==================== 内部 ====================

    /** 从 Spring SecurityContext 获取当前用户角色 */
    private List<String> resolveUserRoles() {
        try {
            return org.springframework.security.core.context.SecurityContextHolder.getContext()
                    .getAuthentication().getAuthorities().stream()
                    .map(Object::toString).collect(Collectors.toList());
        } catch (Exception e) { return Collections.emptyList(); }
    }

    private Map<Long, List<OrderItem>> buildItemMap(List<Long> orderIds) {
        if (orderIds.isEmpty()) return Collections.emptyMap();
        return itemMapper.selectList(new LambdaQueryWrapper<OrderItem>().in(OrderItem::getOrderId, orderIds))
                .stream().collect(Collectors.groupingBy(OrderItem::getOrderId));
    }

    private Map<Long, List<OrderLog>> buildLogMap(List<Long> orderIds) {
        if (orderIds.isEmpty()) return Collections.emptyMap();
        return logMapper.selectList(new LambdaQueryWrapper<OrderLog>().in(OrderLog::getOrderId, orderIds))
                .stream().collect(Collectors.groupingBy(OrderLog::getOrderId));
    }

    private OrderVO toVO(Order o, Map<Long, List<OrderItem>> im, Map<Long, List<OrderLog>> lm) {
        OrderVO vo = new OrderVO();
        vo.setId(String.valueOf(o.getId()));
        vo.setOrderCode(o.getOrderCode());
        vo.setCustomerName(o.getCustomerName());
        vo.setCustomerPhone(o.getCustomerPhone());
        vo.setCustomerAddress(o.getCustomerAddress());

        List<OrderItem> items = im.getOrDefault(o.getId(), Collections.emptyList());
        vo.setItems(items.stream().map(i -> {
            OrderItemVO iv = new OrderItemVO();
            iv.setId(String.valueOf(i.getId()));
            iv.setProductCode(i.getProductCode());
            iv.setProductName(i.getProductName());
            iv.setImageUrl(i.getImageUrl());
            iv.setSpec(i.getSpec());
            iv.setQuantity(i.getQuantity() != null ? i.getQuantity() : 1);
            iv.setPrice(i.getPrice());
            return iv;
        }).collect(Collectors.toList()));

        vo.setTotalAmount(o.getTotalAmount());
        vo.setFreight(o.getFreight());
        vo.setCouponDiscount(o.getCouponDiscount());
        vo.setOrderAmount(o.getOrderAmount());
        vo.setOrderStatus(o.getOrderStatus());
        vo.setPaymentStatus(o.getPaymentStatus());
        vo.setPaymentMethod(o.getPaymentMethod());
        vo.setDeliveryMethod(o.getDeliveryMethod());
        vo.setDeliveryCompany(o.getDeliveryCompany());
        vo.setDeliveryTrackNo(o.getDeliveryTrackNo());
        vo.setDeliveryTime(o.getDeliveryTime() != null ? o.getDeliveryTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) : null);
        vo.setRemark(o.getRemark());
        vo.setCreatedAt(o.getCreatedAt() != null ? o.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) : null);

        List<OrderLog> logs = lm.getOrDefault(o.getId(), Collections.emptyList());
        vo.setLogs(logs.stream().map(l -> {
            OrderLogVO lv = new OrderLogVO(); lv.setTime(l.getTime()); lv.setContent(l.getContent()); return lv;
        }).collect(Collectors.toList()));

        return vo;
    }

    // ---- 辅助 ----
    private String getString(Map<String, Object> m, String key) {
        Object v = m.get(key);
        return v != null ? v.toString() : null;
    }
    private int getInt(Map<String, Object> m, String key, int def) {
        Object v = m.get(key);
        if (v == null) return def;
        return v instanceof Number ? ((Number) v).intValue() : Integer.parseInt(v.toString());
    }
    private BigDecimal getBigDecimal(Map<String, Object> m, String key) {
        Object v = m.get(key);
        if (v == null) return null;
        return v instanceof BigDecimal ? (BigDecimal) v : new BigDecimal(v.toString());
    }
}
