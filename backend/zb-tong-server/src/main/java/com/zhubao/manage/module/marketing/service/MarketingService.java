package com.zhubao.manage.module.marketing.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhubao.manage.common.dto.PageDTO;
import com.zhubao.manage.module.marketing.entity.Activity;
import com.zhubao.manage.module.marketing.entity.Promotion;
import com.zhubao.manage.module.marketing.mapper.ActivityMapper;
import com.zhubao.manage.module.marketing.mapper.PromotionMapper;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MarketingService {

    private final ActivityMapper activityMapper;
    private final PromotionMapper promotionMapper;

    public MarketingService(ActivityMapper am, PromotionMapper pm) { this.activityMapper = am; this.promotionMapper = pm; }

    // ==================== 活动 ====================
    public IPage<Activity> pageActivities(PageDTO dto, String name, String status, String type, String startDate, String endDate) {
        LambdaQueryWrapper<Activity> w = new LambdaQueryWrapper<>();
        if (StringUtils.isNotBlank(name)) w.like(Activity::getName, name);
        if (StringUtils.isNotBlank(status)) w.eq(Activity::getStatus, status);
        if (StringUtils.isNotBlank(type)) w.eq(Activity::getType, type);
        if (StringUtils.isNotBlank(startDate)) w.ge(Activity::getStartTime, startDate);
        if (StringUtils.isNotBlank(endDate)) w.le(Activity::getEndTime, endDate);
        w.orderByDesc(Activity::getCreatedAt);
        return activityMapper.selectPage(new Page<>(dto.getPageNum(), dto.getPageSize()), w);
    }

    public Activity detailActivity(Long id) { return activityMapper.selectById(id); }
    @Transactional public Activity createActivity(Activity a) { activityMapper.insert(a); return a; }
    @Transactional public Activity updateActivity(Long id, Activity a) { a.setId(id); activityMapper.updateById(a); return detailActivity(id); }
    @Transactional public void deleteActivity(Long id) { activityMapper.deleteById(id); }

    // ==================== 优惠 ====================
    public IPage<Promotion> pagePromotions(PageDTO dto, String name, String status, String type, String startDate, String endDate) {
        LambdaQueryWrapper<Promotion> w = new LambdaQueryWrapper<>();
        if (StringUtils.isNotBlank(name)) w.like(Promotion::getName, name);
        if (StringUtils.isNotBlank(status)) w.eq(Promotion::getStatus, status);
        if (StringUtils.isNotBlank(type)) w.eq(Promotion::getType, type);
        if (StringUtils.isNotBlank(startDate)) w.ge(Promotion::getStartTime, startDate);
        if (StringUtils.isNotBlank(endDate)) w.le(Promotion::getEndTime, endDate);
        w.orderByDesc(Promotion::getCreatedAt);
        return promotionMapper.selectPage(new Page<>(dto.getPageNum(), dto.getPageSize()), w);
    }

    public Promotion detailPromotion(Long id) { return promotionMapper.selectById(id); }
    @Transactional public Promotion createPromotion(Promotion p) { promotionMapper.insert(p); return p; }
    @Transactional public Promotion updatePromotion(Long id, Promotion p) { p.setId(id); promotionMapper.updateById(p); return detailPromotion(id); }
    @Transactional public void deletePromotion(Long id) { promotionMapper.deleteById(id); }
}
