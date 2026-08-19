package com.zhubao.manage.module.shop.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.zhubao.manage.common.dto.ApiResult;
import com.zhubao.manage.common.dto.PageDTO;
import com.zhubao.manage.common.dto.PageResult;
import com.zhubao.manage.common.interceptor.UserContextHolder;
import com.zhubao.manage.module.shop.entity.UserCoupon;
import com.zhubao.manage.module.shop.service.ShopCouponService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Api(tags = "商城优惠券")
@RestController
@RequestMapping("/shop/coupons")
@PreAuthorize("isAuthenticated()")
public class ShopCouponController {

    private final ShopCouponService couponService;
    private final UserContextHolder userContextHolder;

    public ShopCouponController(ShopCouponService cs, UserContextHolder uch) {
        this.couponService = cs;
        this.userContextHolder = uch;
    }

    @ApiOperation("可领取的优惠券列表")
    @GetMapping("/available")
    public ApiResult<List<Map<String, Object>>> available() {
        return ApiResult.ok(couponService.availableCoupons(userContextHolder.getUserId()));
    }

    @ApiOperation("领取优惠券")
    @PostMapping("/receive/{promotionId}")
    public ApiResult<UserCoupon> receive(@PathVariable Long promotionId) {
        return ApiResult.ok(couponService.receive(userContextHolder.getUserId(), promotionId));
    }

    @ApiOperation("我的优惠券")
    @GetMapping("/mine")
    public ApiResult<PageResult<UserCoupon>> mine(PageDTO dto,
            @RequestParam(required = false) String status) {
        IPage<UserCoupon> p = couponService.myCoupons(userContextHolder.getUserId(), dto, status);
        return ApiResult.ok(PageResult.of(p));
    }

    @ApiOperation("未使用优惠券数量")
    @GetMapping("/unused-count")
    public ApiResult<Map<String, Object>> unusedCount() {
        Map<String, Object> data = new java.util.LinkedHashMap<>();
        data.put("count", couponService.countUnused(userContextHolder.getUserId()));
        return ApiResult.ok(data);
    }
}
