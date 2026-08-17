package com.zhubao.manage.module.marketing.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.zhubao.manage.common.annotation.OperateLog;
import com.zhubao.manage.common.dto.ApiResult;
import com.zhubao.manage.common.dto.PageDTO;
import com.zhubao.manage.common.dto.PageResult;
import com.zhubao.manage.module.marketing.entity.Activity;
import com.zhubao.manage.module.marketing.entity.Promotion;
import com.zhubao.manage.module.marketing.service.MarketingService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@Api(tags = "营销管理")
@RestController
@RequestMapping
public class MarketingController {

    private final MarketingService marketingService;

    public MarketingController(MarketingService ms) { this.marketingService = ms; }

    // ==================== 活动 ====================

    @ApiOperation("活动分页列表（C端公开）")
    @GetMapping("/activities")
    public ApiResult<PageResult<Activity>> listActivities(@Valid PageDTO dto,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        IPage<Activity> p = marketingService.pageActivities(dto, name, status, type, startDate, endDate);
        return ApiResult.ok(PageResult.of(p));
    }

    @PreAuthorize("isAuthenticated()")
    @OperateLog(module = "营销管理", action = "CREATE", targetType = "ACTIVITY")
    @ApiOperation("新增活动") @PostMapping("/activities")
    public ApiResult<Activity> createActivity(@Valid @RequestBody Activity a) { return ApiResult.ok(marketingService.createActivity(a)); }

    @PreAuthorize("isAuthenticated()")
    @OperateLog(module = "营销管理", action = "UPDATE", targetType = "ACTIVITY", targetIdExpr = "#id")
    @ApiOperation("更新活动") @PutMapping("/activities/{id}")
    public ApiResult<Activity> updateActivity(@PathVariable Long id, @Valid @RequestBody Activity a) { return ApiResult.ok(marketingService.updateActivity(id, a)); }

    @PreAuthorize("isAuthenticated()")
    @OperateLog(module = "营销管理", action = "DELETE", targetType = "ACTIVITY", targetIdExpr = "#id")
    @ApiOperation("删除活动") @DeleteMapping("/activities/{id}")
    public ApiResult<Void> deleteActivity(@PathVariable Long id) { marketingService.deleteActivity(id); return ApiResult.ok(); }

    // ==================== 优惠 ====================

    @ApiOperation("优惠分页列表（C端公开）")
    @GetMapping("/promotions")
    public ApiResult<PageResult<Promotion>> listPromotions(@Valid PageDTO dto,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        IPage<Promotion> p = marketingService.pagePromotions(dto, name, status, type, startDate, endDate);
        return ApiResult.ok(PageResult.of(p));
    }

    @PreAuthorize("isAuthenticated()")
    @OperateLog(module = "营销管理", action = "CREATE", targetType = "PROMOTION")
    @ApiOperation("新增优惠") @PostMapping("/promotions")
    public ApiResult<Promotion> createPromotion(@Valid @RequestBody Promotion p) { return ApiResult.ok(marketingService.createPromotion(p)); }

    @PreAuthorize("isAuthenticated()")
    @OperateLog(module = "营销管理", action = "UPDATE", targetType = "PROMOTION", targetIdExpr = "#id")
    @ApiOperation("更新优惠") @PutMapping("/promotions/{id}")
    public ApiResult<Promotion> updatePromotion(@PathVariable Long id, @Valid @RequestBody Promotion p) { return ApiResult.ok(marketingService.updatePromotion(id, p)); }

    @PreAuthorize("isAuthenticated()")
    @OperateLog(module = "营销管理", action = "DELETE", targetType = "PROMOTION", targetIdExpr = "#id")
    @ApiOperation("删除优惠") @DeleteMapping("/promotions/{id}")
    public ApiResult<Void> deletePromotion(@PathVariable Long id) { marketingService.deletePromotion(id); return ApiResult.ok(); }
}
