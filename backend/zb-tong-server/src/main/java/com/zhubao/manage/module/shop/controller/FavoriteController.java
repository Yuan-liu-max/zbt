package com.zhubao.manage.module.shop.controller;

import com.zhubao.manage.common.dto.ApiResult;
import com.zhubao.manage.common.interceptor.UserContextHolder;
import com.zhubao.manage.module.shop.service.UserFavoriteService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Api(tags = "用户收藏")
@RestController
@RequestMapping("/favorites")
@PreAuthorize("isAuthenticated()")
public class FavoriteController {

    private final UserFavoriteService favoriteService;
    private final UserContextHolder userContextHolder;

    public FavoriteController(UserFavoriteService favoriteService, UserContextHolder uch) {
        this.favoriteService = favoriteService;
        this.userContextHolder = uch;
    }

    @ApiOperation("我的收藏列表")
    @GetMapping
    public ApiResult<List<Map<String, Object>>> list() {
        return ApiResult.ok(favoriteService.list(userContextHolder.getUserId()));
    }

    @ApiOperation("添加收藏")
    @PostMapping
    public ApiResult<Void> add(@RequestBody Map<String, Object> body) {
        Long productId = toLong(body.get("productId"));
        if (productId == null) return ApiResult.fail("商品ID不能为空");
        favoriteService.add(userContextHolder.getUserId(), productId);
        return ApiResult.ok();
    }

    @ApiOperation("取消收藏")
    @DeleteMapping("/{productId}")
    public ApiResult<Void> remove(@PathVariable Long productId) {
        favoriteService.remove(userContextHolder.getUserId(), productId);
        return ApiResult.ok();
    }

    @ApiOperation("是否已收藏")
    @GetMapping("/check/{productId}")
    public ApiResult<Boolean> check(@PathVariable Long productId) {
        return ApiResult.ok(favoriteService.isFavorited(userContextHolder.getUserId(), productId));
    }

    private Long toLong(Object v) {
        if (v == null) return null;
        if (v instanceof Number) return ((Number) v).longValue();
        try { return Long.valueOf(v.toString()); } catch (Exception e) { return null; }
    }
}
