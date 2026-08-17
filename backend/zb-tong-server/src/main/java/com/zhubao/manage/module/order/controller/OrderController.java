package com.zhubao.manage.module.order.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.zhubao.manage.common.annotation.OperateLog;
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

    @ApiOperation("创建订单")
    @OperateLog(module = "订单管理", action = "CREATE", targetType = "ORDER")
    @PostMapping
    public ApiResult<Order> create(@Valid @RequestBody Order order) {
        return ApiResult.ok(orderService.create(order));
    }

    @ApiOperation("更新订单")
    @OperateLog(module = "订单管理", action = "UPDATE", targetType = "ORDER", targetIdExpr = "#id")
    @PutMapping("/{id}")
    public ApiResult<Void> update(@PathVariable Long id, @Valid @RequestBody Order order) {
        orderService.update(id, order); return ApiResult.ok();
    }

    @ApiOperation("取消订单")
    @OperateLog(module = "订单管理", action = "CANCEL", targetType = "ORDER", targetIdExpr = "#id")
    @PutMapping("/{id}/cancel")
    public ApiResult<Void> cancel(@PathVariable Long id) {
        orderService.cancel(id); return ApiResult.ok();
    }

    @ApiOperation("发货 — 填写物流信息并将状态更新为已发货")
    @OperateLog(module = "订单管理", action = "SHIP", targetType = "ORDER", targetIdExpr = "#id")
    @PutMapping("/{id}/ship")
    public ApiResult<Void> ship(@PathVariable Long id, @RequestBody java.util.Map<String, String> body) {
        orderService.ship(id, body.get("deliveryCompany"), body.get("deliveryTrackNo"));
        return ApiResult.ok();
    }

    @ApiOperation("删除订单")
    @OperateLog(module = "订单管理", action = "DELETE", targetType = "ORDER", targetIdExpr = "#id")
    @DeleteMapping("/{id}")
    public ApiResult<Void> delete(@PathVariable Long id) { orderService.delete(id); return ApiResult.ok(); }

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
    @OperateLog(module = "订单管理", action = "CANCEL", targetType = "ORDER_RETURN", targetIdExpr = "#id")
    @PutMapping("/returns/{id}/cancel")
    public ApiResult<Void> cancelReturn(@PathVariable Long id) {
        orderService.cancelReturn(id); return ApiResult.ok();
    }

    @ApiOperation("审核退换货申请（APPLYING → REVIEWING）")
    @OperateLog(module = "订单管理", action = "AUDIT", targetType = "ORDER_RETURN", targetIdExpr = "#id")
    @PutMapping("/returns/{id}/review")
    public ApiResult<Void> reviewReturn(@PathVariable Long id) {
        orderService.reviewReturn(id); return ApiResult.ok();
    }

    @ApiOperation("同意退款/退货（→ APPROVED，订单状态改为 refund）")
    @OperateLog(module = "订单管理", action = "APPROVE", targetType = "ORDER_RETURN", targetIdExpr = "#id")
    @PutMapping("/returns/{id}/approve")
    public ApiResult<Void> approveReturn(@PathVariable Long id) {
        orderService.approveReturn(id); return ApiResult.ok();
    }

    @ApiOperation("拒绝退款/退货（→ REJECTED）")
    @OperateLog(module = "订单管理", action = "REJECT", targetType = "ORDER_RETURN", targetIdExpr = "#id")
    @PutMapping("/returns/{id}/reject")
    public ApiResult<Void> rejectReturn(@PathVariable Long id) {
        orderService.rejectReturn(id); return ApiResult.ok();
    }

    @ApiOperation("确认退款完成（→ COMPLETED，恢复库存，更新支付状态）")
    @OperateLog(module = "订单管理", action = "COMPLETE", targetType = "ORDER_RETURN", targetIdExpr = "#id")
    @PutMapping("/returns/{id}/complete")
    public ApiResult<Void> completeReturn(@PathVariable Long id) {
        orderService.completeReturn(id); return ApiResult.ok();
    }

    @ApiOperation("创建退换货")
    @OperateLog(module = "订单管理", action = "CREATE", targetType = "ORDER_RETURN")
    @PostMapping("/returns")
    public ApiResult<OrderReturn> createReturn(@Valid @RequestBody OrderReturn r) { return ApiResult.ok(orderService.createReturn(r)); }

    @ApiOperation("更新退换货")
    @OperateLog(module = "订单管理", action = "UPDATE", targetType = "ORDER_RETURN", targetIdExpr = "#id")
    @PutMapping("/returns/{id}")
    public ApiResult<Void> updateReturn(@PathVariable Long id, @Valid @RequestBody OrderReturn r) { orderService.updateReturn(id, r); return ApiResult.ok(); }

    @ApiOperation("删除退换货")
    @OperateLog(module = "订单管理", action = "DELETE", targetType = "ORDER_RETURN", targetIdExpr = "#id")
    @DeleteMapping("/returns/{id}")
    public ApiResult<Void> deleteReturn(@PathVariable Long id) { orderService.deleteReturn(id); return ApiResult.ok(); }
}
