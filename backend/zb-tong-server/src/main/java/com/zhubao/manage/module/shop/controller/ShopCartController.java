package com.zhubao.manage.module.shop.controller;

import com.zhubao.manage.common.dto.ApiResult;
import com.zhubao.manage.common.interceptor.UserContextHolder;
import com.zhubao.manage.module.shop.service.ShopCartService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Api(tags = "商城购物车")
@RestController
@RequestMapping("/shop/cart")
@PreAuthorize("isAuthenticated()")
public class ShopCartController {

    private final ShopCartService cartService;
    private final UserContextHolder userContextHolder;

    public ShopCartController(ShopCartService cartService, UserContextHolder uch) {
        this.cartService = cartService;
        this.userContextHolder = uch;
    }

    @ApiOperation("获取购物车列表")
    @GetMapping
    public ApiResult<List<Map<String, Object>>> list() {
        return ApiResult.ok(cartService.list(userContextHolder.getUserId()));
    }

    @ApiOperation("添加商品到购物车")
    @PostMapping
    public ApiResult<Void> add(@RequestBody Map<String, Object> body) {
        Long productId = toLong(body.get("productId"));
        Integer quantity = toInt(body.get("quantity"), 1);
        if (productId == null) return ApiResult.fail("商品ID不能为空");
        cartService.add(userContextHolder.getUserId(), productId, quantity);
        return ApiResult.ok();
    }

    @ApiOperation("更新数量")
    @PutMapping("/{id}")
    public ApiResult<Void> updateQuantity(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        Integer quantity = toInt(body.get("quantity"), 1);
        cartService.updateQuantity(id, userContextHolder.getUserId(), quantity);
        return ApiResult.ok();
    }

    @ApiOperation("勾选/取消勾选")
    @PutMapping("/{id}/check")
    public ApiResult<Void> toggleCheck(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        Integer checked = toInt(body.get("checked"), 1);
        cartService.toggleCheck(id, userContextHolder.getUserId(), checked);
        return ApiResult.ok();
    }

    @ApiOperation("全选/取消全选")
    @PutMapping("/check-all")
    public ApiResult<Void> checkAll(@RequestBody Map<String, Object> body) {
        Integer checked = toInt(body.get("checked"), 1);
        cartService.checkAll(userContextHolder.getUserId(), checked);
        return ApiResult.ok();
    }

    @ApiOperation("删除购物车商品")
    @DeleteMapping("/{id}")
    public ApiResult<Void> remove(@PathVariable Long id) {
        cartService.remove(id, userContextHolder.getUserId());
        return ApiResult.ok();
    }

    @ApiOperation("清空已勾选商品")
    @DeleteMapping
    public ApiResult<Void> removeChecked() {
        cartService.removeChecked(userContextHolder.getUserId());
        return ApiResult.ok();
    }

    @ApiOperation("同步本地购物车到服务端")
    @PostMapping("/sync")
    public ApiResult<List<Map<String, Object>>> sync(@RequestBody List<Map<String, Object>> items) {
        return ApiResult.ok(cartService.sync(userContextHolder.getUserId(), items));
    }

    private Long toLong(Object v) {
        if (v == null) return null;
        if (v instanceof Number) return ((Number) v).longValue();
        try { return Long.valueOf(v.toString()); } catch (Exception e) { return null; }
    }

    private Integer toInt(Object v, Integer def) {
        if (v == null) return def;
        if (v instanceof Number) return ((Number) v).intValue();
        try { return Integer.valueOf(v.toString()); } catch (Exception e) { return def; }
    }
}
