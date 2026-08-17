package com.zhubao.manage.module.role.controller;

import com.zhubao.manage.common.annotation.OperateLog;
import com.zhubao.manage.common.dto.ApiResult;
import com.zhubao.manage.common.dto.PageDTO;
import com.zhubao.manage.common.dto.PageResult;
import com.zhubao.manage.module.role.dto.AssignDataScopeDTO;
import com.zhubao.manage.module.role.dto.AssignPermissionDTO;
import com.zhubao.manage.module.role.dto.RoleCreateDTO;
import com.zhubao.manage.module.role.dto.RoleUpdateDTO;
import com.zhubao.manage.module.role.entity.Permission;
import com.zhubao.manage.module.role.entity.Role;
import com.zhubao.manage.module.role.entity.RoleDataScope;
import com.zhubao.manage.module.role.mapper.PermissionMapper;
import com.zhubao.manage.module.role.service.RoleService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@Api(tags = "角色管理")
@RestController
@RequestMapping("/roles")
@org.springframework.security.access.prepost.PreAuthorize("hasRole('ADMIN')")
public class RoleController {

    private final RoleService roleService;
    private final PermissionMapper permissionMapper;

    public RoleController(RoleService roleService, PermissionMapper permissionMapper) {
        this.roleService = roleService;
        this.permissionMapper = permissionMapper;
    }

    @ApiOperation("角色列表（分页）")
    @GetMapping
    public ApiResult<PageResult<Role>> list(@Valid PageDTO dto) {
        List<Role> all = roleService.listAll();
        int start = (int) ((dto.getPageNum() - 1) * dto.getPageSize());
        List<Role> page = all.stream().skip(start).limit(dto.getPageSize())
                .collect(java.util.stream.Collectors.toList());
        return ApiResult.ok(new PageResult<>(dto.getPageNum(), dto.getPageSize(), (long) all.size(), page));
    }

    @ApiOperation("角色详情")
    @GetMapping("/{id}")
    public ApiResult<Role> detail(@PathVariable Long id) {
        return ApiResult.ok(roleService.detail(id));
    }

    @ApiOperation("新增角色")
    @OperateLog(module = "角色管理", action = "CREATE", targetType = "ROLE")
    @PostMapping
    public ApiResult<Role> create(@Valid @RequestBody RoleCreateDTO dto) {
        return ApiResult.ok(roleService.create(dto));
    }

    @ApiOperation("更新角色")
    @OperateLog(module = "角色管理", action = "UPDATE", targetType = "ROLE", targetIdExpr = "#id")
    @PutMapping("/{id}")
    public ApiResult<Role> update(@PathVariable Long id, @Valid @RequestBody RoleUpdateDTO dto) {
        return ApiResult.ok(roleService.update(id, dto));
    }

    @ApiOperation("删除角色")
    @OperateLog(module = "角色管理", action = "DELETE", targetType = "ROLE", targetIdExpr = "#id")
    @DeleteMapping("/{id}")
    public ApiResult<Void> delete(@PathVariable Long id) {
        roleService.delete(id);
        return ApiResult.ok();
    }

    @ApiOperation("分配权限")
    @OperateLog(module = "角色管理", action = "ASSIGN_PERMISSION", targetType = "ROLE", targetIdExpr = "#id")
    @PutMapping("/{id}/permissions")
    public ApiResult<Void> assignPermissions(@PathVariable Long id, @Valid @RequestBody AssignPermissionDTO dto) {
        dto.setRoleId(id);
        roleService.assignPermissions(id, dto.getPermissionIds());
        return ApiResult.ok();
    }

    @ApiOperation("获取角色的权限列表")
    @GetMapping("/{id}/permissions")
    public ApiResult<List<Permission>> getPermissions(@PathVariable Long id) {
        List<Long> permIds = roleService.getPermissionIds(id);
        if (permIds.isEmpty()) return ApiResult.ok(java.util.Collections.emptyList());
        return ApiResult.ok(permissionMapper.selectBatchIds(permIds));
    }

    @ApiOperation("配置数据权限")
    @OperateLog(module = "角色管理", action = "UPDATE", targetType = "ROLE", targetIdExpr = "#id")
    @PutMapping("/{id}/data-scope")
    public ApiResult<Void> configDataScope(@PathVariable Long id, @Valid @RequestBody AssignDataScopeDTO dto) {
        roleService.configDataScope(id, dto);
        return ApiResult.ok();
    }

    @ApiOperation("获取角色的数据权限")
    @GetMapping("/{id}/data-scope")
    public ApiResult<List<RoleDataScope>> getDataScopes(@PathVariable Long id) {
        return ApiResult.ok(roleService.getDataScopes(id));
    }
}
