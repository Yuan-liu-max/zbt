package com.zhubao.manage.module.role.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zhubao.manage.common.exception.BusinessException;
import com.zhubao.manage.common.exception.ErrorCode;
import com.zhubao.manage.module.role.dto.AssignDataScopeDTO;
import com.zhubao.manage.module.role.dto.RoleCreateDTO;
import com.zhubao.manage.module.role.dto.RoleUpdateDTO;
import com.zhubao.manage.module.role.entity.*;
import com.zhubao.manage.module.role.mapper.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class RoleService {

    private final RoleMapper roleMapper;
    private final RolePermissionMapper rolePermissionMapper;
    private final RoleDataScopeMapper roleDataScopeMapper;

    public RoleService(RoleMapper roleMapper, RolePermissionMapper rolePermissionMapper,
                       RoleDataScopeMapper roleDataScopeMapper) {
        this.roleMapper = roleMapper;
        this.rolePermissionMapper = rolePermissionMapper;
        this.roleDataScopeMapper = roleDataScopeMapper;
    }

    @Transactional(readOnly = true)
    public List<Role> listAll() {
        return roleMapper.selectList(new LambdaQueryWrapper<Role>().orderByDesc(Role::getCreatedAt));
    }

    @Transactional(readOnly = true)
    public Role detail(Long id) {
        return getById(id);
    }

    @Transactional
    public Role create(RoleCreateDTO dto) {
        // 编码唯一
        Long exist = roleMapper.selectCount(
                new LambdaQueryWrapper<Role>().eq(Role::getRoleCode, dto.getRoleCode()));
        if (exist > 0) {
            throw new BusinessException(ErrorCode.ROLE_CODE_EXISTS);
        }
        Role role = new Role();
        role.setRoleCode(dto.getRoleCode());
        role.setRoleName(dto.getRoleName());
        role.setDataScope(dto.getDataScope());
        role.setRemark(dto.getRemark());
        role.setStatus("ENABLED");
        roleMapper.insert(role);
        return role;
    }

    @Transactional
    public Role update(Long id, RoleUpdateDTO dto) {
        Role role = getById(id);
        if (dto.getRoleName() != null) role.setRoleName(dto.getRoleName());
        if (dto.getDataScope() != null) role.setDataScope(dto.getDataScope());
        if (dto.getStatus() != null) role.setStatus(dto.getStatus());
        if (dto.getRemark() != null) role.setRemark(dto.getRemark());
        roleMapper.updateById(role);
        return role;
    }

    @Transactional
    public void delete(Long id) {
        Role role = getById(id);
        if (isBuiltinRole(role.getRoleCode())) {
            throw new BusinessException(ErrorCode.ROLE_BUILTIN);
        }
        roleMapper.deleteById(id);
        // 清除关联数据
        rolePermissionMapper.delete(new LambdaQueryWrapper<RolePermission>().eq(RolePermission::getRoleId, id));
        roleDataScopeMapper.delete(new LambdaQueryWrapper<RoleDataScope>().eq(RoleDataScope::getRoleId, id));
    }

    /**
     * 系统预置角色（ROLE_ADMIN/ROLE_HQ/... 前缀）禁止删除
     */
    private boolean isBuiltinRole(String roleCode) {
        if (roleCode == null) return false;
        String[] prefixes = {"ROLE_ADMIN", "ROLE_HQ", "ROLE_REGIONAL", "ROLE_MANAGER", "ROLE_ASSOCIATE"};
        for (String prefix : prefixes) {
            if (roleCode.startsWith(prefix)) return true;
        }
        return false;
    }

    /**
     * 分配权限 —— 全量替换
     */
    @Transactional
    public void assignPermissions(Long roleId, List<Long> permissionIds) {
        getById(roleId);
        rolePermissionMapper.delete(new LambdaQueryWrapper<RolePermission>().eq(RolePermission::getRoleId, roleId));
        for (Long permId : permissionIds) {
            RolePermission rp = new RolePermission();
            rp.setRoleId(roleId);
            rp.setPermissionId(permId);
            rolePermissionMapper.insert(rp);
        }
    }

    /**
     * 获取角色的权限ID列表
     */
    @Transactional(readOnly = true)
    public List<Long> getPermissionIds(Long roleId) {
        return rolePermissionMapper.selectList(
                new LambdaQueryWrapper<RolePermission>().eq(RolePermission::getRoleId, roleId))
                .stream().map(RolePermission::getPermissionId).collect(java.util.stream.Collectors.toList());
    }

    /**
     * 配置数据权限 —— 全量替换
     */
    @Transactional
    public void configDataScope(Long roleId, AssignDataScopeDTO dto) {
        getById(roleId);
        roleDataScopeMapper.delete(new LambdaQueryWrapper<RoleDataScope>().eq(RoleDataScope::getRoleId, roleId));
        for (AssignDataScopeDTO.ScopeItem item : dto.getScopes()) {
            RoleDataScope scope = new RoleDataScope();
            scope.setRoleId(roleId);
            scope.setScopeType(item.getScopeType());
            scope.setScopeValue(item.getScopeValue());
            roleDataScopeMapper.insert(scope);
        }
    }

    /**
     * 获取角色的数据权限配置
     */
    @Transactional(readOnly = true)
    public List<RoleDataScope> getDataScopes(Long roleId) {
        return roleDataScopeMapper.selectList(
                new LambdaQueryWrapper<RoleDataScope>().eq(RoleDataScope::getRoleId, roleId));
    }

    private Role getById(Long id) {
        Role role = roleMapper.selectById(id);
        if (role == null) {
            throw new BusinessException(ErrorCode.ROLE_NOT_FOUND);
        }
        return role;
    }
}
