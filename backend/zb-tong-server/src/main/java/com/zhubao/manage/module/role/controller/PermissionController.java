package com.zhubao.manage.module.role.controller;

import com.zhubao.manage.common.dto.ApiResult;
import com.zhubao.manage.module.role.entity.Permission;
import com.zhubao.manage.module.role.service.PermissionService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@Api(tags = "权限管理")
@RestController
@RequestMapping("/permissions")
@org.springframework.security.access.prepost.PreAuthorize("hasRole('ADMIN')")
public class PermissionController {

    private final PermissionService permissionService;

    public PermissionController(PermissionService permissionService) {
        this.permissionService = permissionService;
    }

    @ApiOperation("权限树")
    @GetMapping("/tree")
    public ApiResult<List<Map<String, Object>>> tree() {
        return ApiResult.ok(permissionService.tree());
    }

    @ApiOperation("权限列表（平铺）")
    @GetMapping
    public ApiResult<List<Permission>> list() {
        return ApiResult.ok(permissionService.listAll());
    }
}
