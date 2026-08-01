package com.zhubao.manage.module.organization.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
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

@Api(tags = "门店管理")
@RestController
@RequestMapping("/stores")
@PreAuthorize("hasAnyRole('ADMIN','HQ')")
public class StoreController {

    private final StoreService storeService;

    public StoreController(StoreService storeService) { this.storeService = storeService; }

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

    @ApiOperation("新增门店")
    @PostMapping
    public ApiResult<Store> create(@Valid @RequestBody Store store) {
        return ApiResult.ok(storeService.create(store));
    }

    @ApiOperation("更新门店")
    @PutMapping("/{id}")
    public ApiResult<Store> update(@PathVariable Long id, @Valid @RequestBody Store store) {
        return ApiResult.ok(storeService.update(id, store));
    }

    @ApiOperation("删除门店")
    @DeleteMapping("/{id}")
    public ApiResult<Void> delete(@PathVariable Long id) {
        storeService.delete(id);
        return ApiResult.ok();
    }
}
