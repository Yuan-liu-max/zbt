package com.zhubao.manage.module.order.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.zhubao.manage.common.dto.ApiResult;
import com.zhubao.manage.common.dto.PageResult;
import com.zhubao.manage.module.order.dto.*;
import com.zhubao.manage.module.order.entity.*;
import com.zhubao.manage.module.order.service.OrderService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@Api(tags = "订单管理")
@RestController
@RequestMapping("/orders")
@PreAuthorize("isAuthenticated()")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) { this.orderService = orderService; }

    @ApiOperation("订单分页列表（含 items + logs）")
    @GetMapping
    public ApiResult<PageResult<OrderVO>> list(@Valid OrderQueryDTO query) {
        IPage<OrderVO> p = orderService.pageOrders(query);
        return ApiResult.ok(PageResult.of(p));
    }

    @ApiOperation("订单详情（含 items + logs）")
    @GetMapping("/{id}")
    public ApiResult<OrderVO> detail(@PathVariable Long id) {
        return ApiResult.ok(orderService.detail(id));
    }

    @ApiOperation("更新订单")
    @PutMapping("/{id}")
    public ApiResult<Void> update(@PathVariable Long id, @RequestBody Order order) {
        orderService.update(id, order); return ApiResult.ok();
    }

    @ApiOperation("取消订单")
    @PutMapping("/{id}/cancel")
    public ApiResult<Void> cancel(@PathVariable Long id) {
        orderService.cancel(id); return ApiResult.ok();
    }

    @ApiOperation("退换货分页列表")
    @GetMapping("/returns")
    public ApiResult<PageResult<OrderReturn>> listReturns(@Valid ReturnQueryDTO query) {
        IPage<OrderReturn> p = orderService.pageReturns(query);
        return ApiResult.ok(PageResult.of(p));
    }

    @ApiOperation("退换货详情")
    @GetMapping("/returns/{id}")
    public ApiResult<OrderReturn> detailReturn(@PathVariable Long id) {
        return ApiResult.ok(orderService.detailReturn(id));
    }

    @ApiOperation("撤销退换货申请")
    @PutMapping("/returns/{id}/cancel")
    public ApiResult<Void> cancelReturn(@PathVariable Long id) {
        orderService.cancelReturn(id); return ApiResult.ok();
    }
}
