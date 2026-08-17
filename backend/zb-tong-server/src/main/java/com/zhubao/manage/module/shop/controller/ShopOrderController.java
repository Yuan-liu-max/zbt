package com.zhubao.manage.module.shop.controller;

import com.zhubao.manage.common.dto.ApiResult;
import com.zhubao.manage.common.dto.PageDTO;
import com.zhubao.manage.common.dto.PageResult;
import com.zhubao.manage.common.interceptor.UserContextHolder;
import com.zhubao.manage.module.order.dto.OrderVO;
import com.zhubao.manage.module.order.entity.Order;
import com.zhubao.manage.module.order.entity.OrderReturn;
import com.zhubao.manage.module.shop.dto.CreateOrderRequest;
import com.zhubao.manage.module.shop.service.ShopOrderService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.math.BigDecimal;
import java.util.Map;

@Api(tags = "商城订单")
@RestController
@RequestMapping("/shop/orders")
@PreAuthorize("isAuthenticated()")
public class ShopOrderController {

    private final ShopOrderService shopOrderService;
    private final UserContextHolder userContextHolder;

    public ShopOrderController(ShopOrderService shopOrderService, UserContextHolder uch) {
        this.shopOrderService = shopOrderService;
        this.userContextHolder = uch;
    }

    @ApiOperation("提交订单")
    @PostMapping
    public ApiResult<Order> create(@RequestBody CreateOrderRequest req) {
        return ApiResult.ok(shopOrderService.createOrder(userContextHolder.getUserId(), req));
    }

    @ApiOperation("我的订单列表")
    @GetMapping
    public ApiResult<PageResult<OrderVO>> list(@Valid PageDTO dto,
                                                @RequestParam(required = false) String status) {
        return ApiResult.ok(PageResult.of(shopOrderService.pageMyOrders(userContextHolder.getUserId(), dto, status)));
    }

    @ApiOperation("订单详情")
    @GetMapping("/{id}")
    public ApiResult<OrderVO> detail(@PathVariable Long id) {
        OrderVO vo = shopOrderService.detail(userContextHolder.getUserId(), id);
        if (vo == null) return ApiResult.fail("订单不存在");
        return ApiResult.ok(vo);
    }

    @ApiOperation("取消订单")
    @PutMapping("/{id}/cancel")
    public ApiResult<Void> cancel(@PathVariable Long id) {
        shopOrderService.cancelOrder(userContextHolder.getUserId(), id);
        return ApiResult.ok();
    }

    @ApiOperation("模拟支付（开发阶段）")
    @PutMapping("/{id}/pay")
    public ApiResult<Order> pay(@PathVariable Long id) {
        return ApiResult.ok(shopOrderService.payOrder(userContextHolder.getUserId(), id));
    }

    @ApiOperation("确认收货")
    @PutMapping("/{id}/confirm-receive")
    public ApiResult<Void> confirmReceive(@PathVariable Long id) {
        shopOrderService.confirmReceive(userContextHolder.getUserId(), id);
        return ApiResult.ok();
    }

    @ApiOperation("申请退货/退款")
    @PostMapping("/{id}/apply-return")
    public ApiResult<OrderReturn> applyReturn(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        String reason = body != null ? (String) body.getOrDefault("reason", "") : "";
        BigDecimal refundAmount = null;
        if (body != null && body.get("refundAmount") != null) {
            Object amt = body.get("refundAmount");
            refundAmount = amt instanceof BigDecimal ? (BigDecimal) amt : new BigDecimal(amt.toString());
        }
        return ApiResult.ok(shopOrderService.applyReturn(userContextHolder.getUserId(), id, reason, refundAmount));
    }
}
