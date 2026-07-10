package com.zhubao.manage.module.organization.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zhubao.manage.common.exception.BusinessException;
import com.zhubao.manage.common.exception.ErrorCode;
import com.zhubao.manage.module.organization.entity.Organization;
import com.zhubao.manage.module.organization.mapper.OrganizationMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class OrganizationService {

    private final OrganizationMapper organizationMapper;

    public OrganizationService(OrganizationMapper organizationMapper) {
        this.organizationMapper = organizationMapper;
    }

    /**
     * 组织树 —— 递归查询全部并构建树
     */
    @Transactional(readOnly = true)
    public List<Map<String, Object>> tree() {
        List<Organization> all = organizationMapper.selectList(
                new LambdaQueryWrapper<Organization>()
                        .orderByAsc(Organization::getSortOrder));

        Map<Long, List<Organization>> childrenMap = all.stream()
                .filter(o -> o.getParentId() != null && o.getParentId() > 0)
                .collect(Collectors.groupingBy(Organization::getParentId));

        return all.stream()
                .filter(o -> o.getParentId() == null || o.getParentId() == 0)
                .map(root -> buildNode(root, childrenMap))
                .collect(Collectors.toList());
    }

    private Map<String, Object> buildNode(Organization org, Map<Long, List<Organization>> childrenMap) {
        Map<String, Object> node = new LinkedHashMap<>();
        node.put("id", org.getId());
        node.put("parentId", org.getParentId());
        node.put("orgName", org.getOrgName());
        node.put("orgType", org.getOrgType());
        node.put("orgCode", org.getOrgCode());
        node.put("sortOrder", org.getSortOrder());
        node.put("status", org.getStatus());

        List<Organization> children = childrenMap.getOrDefault(org.getId(), Collections.emptyList());
        node.put("children", children.stream()
                .map(c -> buildNode(c, childrenMap))
                .collect(Collectors.toList()));

        return node;
    }

    /**
     * 查询全部（平铺）
     */
    @Transactional(readOnly = true)
    public List<Organization> listAll() {
        return organizationMapper.selectList(
                new LambdaQueryWrapper<Organization>()
                        .orderByAsc(Organization::getSortOrder));
    }

    /**
     * 创建组织
     */
    @Transactional
    public Organization create(Organization org) {
        organizationMapper.insert(org);
        return org;
    }

    /**
     * 更新组织
     */
    @Transactional
    public Organization update(Long id, Organization org) {
        Organization exist = getById(id);
        org.setId(id);
        organizationMapper.updateById(org);
        return getById(id);
    }

    /**
     * 删除组织（逻辑删除）
     */
    @Transactional
    public void delete(Long id) {
        Organization org = getById(id);
        // 检查子节点
        Long childCount = organizationMapper.selectCount(
                new LambdaQueryWrapper<Organization>().eq(Organization::getParentId, id));
        if (childCount > 0) {
            throw new BusinessException(ErrorCode.ORG_HAS_CHILDREN);
        }
        organizationMapper.deleteById(id);
    }

    @Transactional(readOnly = true)
    public Organization getById(Long id) {
        Organization org = organizationMapper.selectById(id);
        if (org == null) {
            throw new BusinessException(ErrorCode.ORG_NOT_FOUND);
        }
        return org;
    }
}
