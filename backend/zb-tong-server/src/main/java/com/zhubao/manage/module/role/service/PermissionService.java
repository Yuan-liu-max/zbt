package com.zhubao.manage.module.role.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zhubao.manage.module.role.entity.Permission;
import com.zhubao.manage.module.role.mapper.PermissionMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class PermissionService {

    private final PermissionMapper permissionMapper;

    public PermissionService(PermissionMapper permissionMapper) {
        this.permissionMapper = permissionMapper;
    }

    /**
     * 权限树
     */
    @Transactional(readOnly = true)
    public List<Map<String, Object>> tree() {
        List<Permission> all = permissionMapper.selectList(
                new LambdaQueryWrapper<Permission>()
                        .eq(Permission::getStatus, "ENABLED")
                        .orderByAsc(Permission::getSortOrder));

        Map<Long, List<Permission>> childrenMap = all.stream()
                .filter(p -> p.getParentId() != null && p.getParentId() > 0)
                .collect(Collectors.groupingBy(Permission::getParentId));

        return all.stream()
                .filter(p -> p.getParentId() == null || p.getParentId() == 0)
                .map(root -> buildNode(root, childrenMap))
                .collect(Collectors.toList());
    }

    /**
     * 权限列表（平铺）
     */
    @Transactional(readOnly = true)
    public List<Permission> listAll() {
        return permissionMapper.selectList(
                new LambdaQueryWrapper<Permission>()
                        .orderByAsc(Permission::getSortOrder));
    }

    private Map<String, Object> buildNode(Permission perm, Map<Long, List<Permission>> childrenMap) {
        Map<String, Object> node = new LinkedHashMap<>();
        node.put("id", perm.getId());
        node.put("parentId", perm.getParentId());
        node.put("permName", perm.getPermName());
        node.put("permType", perm.getPermType());
        node.put("permCode", perm.getPermCode());
        node.put("path", perm.getPath());
        node.put("component", perm.getComponent());
        node.put("icon", perm.getIcon());
        node.put("sortOrder", perm.getSortOrder());

        List<Permission> children = childrenMap.getOrDefault(perm.getId(), Collections.emptyList());
        node.put("children", children.stream()
                .map(c -> buildNode(c, childrenMap))
                .collect(Collectors.toList()));

        return node;
    }
}
