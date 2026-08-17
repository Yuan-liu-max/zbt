package com.zhubao.manage.module.supplier.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.zhubao.manage.common.annotation.OperateLog;
import com.zhubao.manage.common.dto.ApiResult;
import com.zhubao.manage.common.dto.PageDTO;
import com.zhubao.manage.common.dto.PageResult;
import com.zhubao.manage.module.supplier.entity.Supplier;
import com.zhubao.manage.module.supplier.service.SupplierService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@Api(tags = "供应商管理")
@RestController
@RequestMapping("/suppliers")
@PreAuthorize("isAuthenticated()")
public class SupplierController {

    private final SupplierService supplierService;

    public SupplierController(SupplierService ss) { this.supplierService = ss; }

    @ApiOperation("供应商分页列表")
    @GetMapping
    public ApiResult<PageResult<Supplier>> list(@Valid PageDTO dto,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String contactPerson,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String status) {
        IPage<Supplier> p = supplierService.page(dto, name, contactPerson, type, status);
        return ApiResult.ok(PageResult.of(p));
    }

    @ApiOperation("供应商详情") @GetMapping("/{id}")
    public ApiResult<Supplier> detail(@PathVariable Long id) { return ApiResult.ok(supplierService.detail(id)); }

    @OperateLog(module = "供应商管理", action = "CREATE", targetType = "SUPPLIER")
    @ApiOperation("新增供应商") @PostMapping
    public ApiResult<Supplier> create(@Valid @RequestBody Supplier s) { return ApiResult.ok(supplierService.create(s)); }

    @OperateLog(module = "供应商管理", action = "UPDATE", targetType = "SUPPLIER", targetIdExpr = "#id")
    @ApiOperation("更新供应商") @PutMapping("/{id}")
    public ApiResult<Supplier> update(@PathVariable Long id, @Valid @RequestBody Supplier s) { return ApiResult.ok(supplierService.update(id, s)); }

    @OperateLog(module = "供应商管理", action = "DELETE", targetType = "SUPPLIER", targetIdExpr = "#id")
    @ApiOperation("删除供应商") @DeleteMapping("/{id}")
    public ApiResult<Void> delete(@PathVariable Long id) { supplierService.delete(id); return ApiResult.ok(); }
}
