package com.zhubao.manage.module.organization.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zhubao.manage.common.annotation.OperateLog;
import com.zhubao.manage.common.dto.ApiResult;
import com.zhubao.manage.common.dto.PageDTO;
import com.zhubao.manage.common.dto.PageResult;
import com.zhubao.manage.module.organization.entity.Organization;
import com.zhubao.manage.module.organization.service.OrganizationService;
import com.zhubao.manage.module.user.entity.User;
import com.zhubao.manage.module.user.mapper.UserMapper;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Api(tags = "组织管理")
@RestController
@RequestMapping("/organizations")
@org.springframework.security.access.prepost.PreAuthorize("hasRole('ADMIN')")
public class OrganizationController {

    private final OrganizationService organizationService;
    private final UserMapper userMapper;

    public OrganizationController(OrganizationService organizationService, UserMapper userMapper) {
        this.organizationService = organizationService;
        this.userMapper = userMapper;
    }

    @ApiOperation("获取组织树")
    @GetMapping("/tree")
    public ApiResult<List<Map<String, Object>>> tree() {
        return ApiResult.ok(organizationService.tree());
    }

    @ApiOperation("组织列表（平铺，分页，含成员数）")
    @GetMapping
    public ApiResult<PageResult<Organization>> list(@Valid PageDTO dto) {
        List<Organization> all = organizationService.listAll();
        // 填充 memberCount：按 storeId 或 regionId 统计用户数
        List<User> allUsers = userMapper.selectList(new LambdaQueryWrapper<User>().eq(User::getIsDeleted, 0));
        Map<Long, Long> storeUserCount = allUsers.stream()
                .filter(u -> u.getStoreId() != null)
                .collect(Collectors.groupingBy(User::getStoreId, Collectors.counting()));
        Map<Long, Long> regionUserCount = allUsers.stream()
                .filter(u -> u.getRegionId() != null)
                .collect(Collectors.groupingBy(User::getRegionId, Collectors.counting()));
        for (Organization org : all) {
            long count = 0;
            if ("STORE".equals(org.getOrgType())) {
                count = storeUserCount.getOrDefault(org.getId(), 0L);
            } else {
                count = regionUserCount.getOrDefault(org.getId(), 0L);
            }
            org.setMemberCount((int) count);
        }
        int start = (int) ((dto.getPageNum() - 1) * dto.getPageSize());
        List<Organization> page = all.stream().skip(start).limit(dto.getPageSize())
                .collect(Collectors.toList());
        return ApiResult.ok(new PageResult<>(dto.getPageNum(), dto.getPageSize(), (long) all.size(), page));
    }

    @ApiOperation("组织详情")
    @GetMapping("/{id}")
    public ApiResult<Organization> detail(@PathVariable Long id) {
        return ApiResult.ok(organizationService.getById(id));
    }

    @OperateLog(module = "组织管理", action = "CREATE", targetType = "ORGANIZATION")
    @ApiOperation("新增组织")
    @PostMapping
    public ApiResult<Organization> create(@Valid @RequestBody Organization org) {
        return ApiResult.ok(organizationService.create(org));
    }

    @OperateLog(module = "组织管理", action = "UPDATE", targetType = "ORGANIZATION", targetIdExpr = "#id")
    @ApiOperation("更新组织")
    @PutMapping("/{id}")
    public ApiResult<Organization> update(@PathVariable Long id, @Valid @RequestBody Organization org) {
        return ApiResult.ok(organizationService.update(id, org));
    }

    @OperateLog(module = "组织管理", action = "DELETE", targetType = "ORGANIZATION", targetIdExpr = "#id")
    @ApiOperation("删除组织")
    @DeleteMapping("/{id}")
    public ApiResult<Void> delete(@PathVariable Long id) {
        organizationService.delete(id);
        return ApiResult.ok();
    }
}
