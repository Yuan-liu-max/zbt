package com.zhubao.manage.module.organization.controller;

import com.zhubao.manage.common.dto.ApiResult;
import com.zhubao.manage.module.organization.entity.Organization;
import com.zhubao.manage.module.organization.service.OrganizationService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.Map;

@Api(tags = "组织管理")
@RestController
@RequestMapping("/organizations")
public class OrganizationController {

    private final OrganizationService organizationService;

    public OrganizationController(OrganizationService organizationService) {
        this.organizationService = organizationService;
    }

    @ApiOperation("获取组织树")
    @GetMapping("/tree")
    public ApiResult<List<Map<String, Object>>> tree() {
        return ApiResult.ok(organizationService.tree());
    }

    @ApiOperation("组织列表（平铺）")
    @GetMapping
    public ApiResult<List<Organization>> list() {
        return ApiResult.ok(organizationService.listAll());
    }

    @ApiOperation("组织详情")
    @GetMapping("/{id}")
    public ApiResult<Organization> detail(@PathVariable Long id) {
        return ApiResult.ok(organizationService.getById(id));
    }

    @ApiOperation("新增组织")
    @PostMapping
    public ApiResult<Organization> create(@Valid @RequestBody Organization org) {
        return ApiResult.ok(organizationService.create(org));
    }

    @ApiOperation("更新组织")
    @PutMapping("/{id}")
    public ApiResult<Organization> update(@PathVariable Long id, @Valid @RequestBody Organization org) {
        return ApiResult.ok(organizationService.update(id, org));
    }

    @ApiOperation("删除组织")
    @DeleteMapping("/{id}")
    public ApiResult<Void> delete(@PathVariable Long id) {
        organizationService.delete(id);
        return ApiResult.ok();
    }
}
