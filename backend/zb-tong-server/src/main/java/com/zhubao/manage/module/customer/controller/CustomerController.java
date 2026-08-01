package com.zhubao.manage.module.customer.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.zhubao.manage.common.dto.ApiResult;
import com.zhubao.manage.common.dto.PageDTO;
import com.zhubao.manage.common.dto.PageResult;
import com.zhubao.manage.module.customer.entity.Customer;
import com.zhubao.manage.module.customer.entity.MemberLevel;
import com.zhubao.manage.module.customer.service.CustomerService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Map;

@Api(tags = "客户管理")
@RestController
@RequestMapping
@PreAuthorize("isAuthenticated()")
public class CustomerController {

    private final CustomerService customerService;

    public CustomerController(CustomerService cs) { this.customerService = cs; }

    // ==================== 客户 CRUD ====================

    @ApiOperation("客户分页列表")
    @GetMapping("/customers")
    public ApiResult<PageResult<Customer>> listCustomers(@Valid PageDTO dto,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String phone,
            @RequestParam(required = false) String level,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        IPage<Customer> p = customerService.pageCustomers(dto, name, phone, level, startDate, endDate);
        return ApiResult.ok(PageResult.of(p));
    }

    @ApiOperation("客户详情") @GetMapping("/customers/{id}")
    public ApiResult<Customer> getCustomer(@PathVariable Long id) { return ApiResult.ok(customerService.detail(id)); }

    @ApiOperation("新增客户") @PostMapping("/customers")
    public ApiResult<Customer> createCustomer(@Valid @RequestBody Customer c) { return ApiResult.ok(customerService.create(c)); }

    @ApiOperation("更新客户") @PutMapping("/customers/{id}")
    public ApiResult<Customer> updateCustomer(@PathVariable Long id, @Valid @RequestBody Customer c) { return ApiResult.ok(customerService.update(id, c)); }

    @ApiOperation("删除客户") @DeleteMapping("/customers/{id}")
    public ApiResult<Void> deleteCustomer(@PathVariable Long id) { customerService.delete(id); return ApiResult.ok(); }

    // ==================== 会员等级 ====================

    @ApiOperation("会员等级分页")
    @GetMapping("/member-levels")
    public ApiResult<PageResult<MemberLevel>> listLevels(@Valid PageDTO dto,
            @RequestParam(required = false) String level,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String phone,
            @RequestParam(required = false) String status) {
        IPage<MemberLevel> p = customerService.pageLevels(dto, level, name, status);
        return ApiResult.ok(PageResult.of(p));
    }

    @ApiOperation("会员等级统计")
    @GetMapping("/member-levels/stats")
    public ApiResult<Map<String, Object>> levelStats() { return ApiResult.ok(customerService.levelStats()); }

    @ApiOperation("新增会员等级") @PostMapping("/member-levels")
    public ApiResult<MemberLevel> createLevel(@Valid @RequestBody MemberLevel ml) { return ApiResult.ok(customerService.createLevel(ml)); }

    @ApiOperation("更新会员等级") @PutMapping("/member-levels/{id}")
    public ApiResult<MemberLevel> updateLevel(@PathVariable Long id, @Valid @RequestBody MemberLevel ml) { return ApiResult.ok(customerService.updateLevel(id, ml)); }
}
