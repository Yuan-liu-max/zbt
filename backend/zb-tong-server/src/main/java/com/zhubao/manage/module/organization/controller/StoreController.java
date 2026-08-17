package com.zhubao.manage.module.organization.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.zhubao.manage.common.annotation.OperateLog;
import com.zhubao.manage.common.dto.ApiResult;
import com.zhubao.manage.common.dto.PageDTO;
import com.zhubao.manage.common.dto.PageResult;
import com.zhubao.manage.module.organization.entity.Store;
import com.zhubao.manage.module.organization.service.StoreService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@Api(tags = "门店管理")
@RestController
@RequestMapping("/stores")
public class StoreController {

    private final StoreService storeService;

    public StoreController(StoreService storeService) { this.storeService = storeService; }

    @ApiOperation("全部门店（id/name 简表，供下拉选择）")
    @GetMapping("/all")
    @PreAuthorize("isAuthenticated()")
    public ApiResult<List<java.util.Map<String, Object>>> all() {
        return ApiResult.ok(storeService.listAll().stream()
                .map(s -> {
                    java.util.Map<String, Object> m = new java.util.LinkedHashMap<>();
                    m.put("id", s.getId());
                    m.put("name", s.getStoreName());
                    m.put("storeCode", s.getStoreCode());
                    return m;
                })
                .collect(java.util.stream.Collectors.toList()));
    }

    @ApiOperation("门店分页列表")
    @GetMapping
    public ApiResult<PageResult<Store>> page(@Valid PageDTO dto, @RequestParam(required = false) String keyword) {
        IPage<Store> r = storeService.page(dto, keyword);
        return ApiResult.ok(PageResult.of(r));
    }

    @ApiOperation("门店详情")
    @GetMapping("/{id}")
    public ApiResult<Store> detail(@PathVariable Long id) {
        return ApiResult.ok(storeService.detail(id));
    }

    @PreAuthorize("hasAnyRole('ADMIN','HQ')")
    @OperateLog(module = "门店管理", action = "CREATE", targetType = "STORE")
    @ApiOperation("新增门店")
    @PostMapping
    public ApiResult<Store> create(@Valid @RequestBody Store store) {
        return ApiResult.ok(storeService.create(store));
    }

    @PreAuthorize("hasAnyRole('ADMIN','HQ')")
    @OperateLog(module = "门店管理", action = "UPDATE", targetType = "STORE", targetIdExpr = "#id")
    @ApiOperation("更新门店")
    @PutMapping("/{id}")
    public ApiResult<Store> update(@PathVariable Long id, @Valid @RequestBody Store store) {
        return ApiResult.ok(storeService.update(id, store));
    }

    @PreAuthorize("hasAnyRole('ADMIN','HQ')")
    @OperateLog(module = "门店管理", action = "DELETE", targetType = "STORE", targetIdExpr = "#id")
    @ApiOperation("删除门店")
    @DeleteMapping("/{id}")
    public ApiResult<Void> delete(@PathVariable Long id) {
        storeService.delete(id);
        return ApiResult.ok();
    }
}
