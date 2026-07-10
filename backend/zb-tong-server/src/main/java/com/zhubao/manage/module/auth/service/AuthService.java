package com.zhubao.manage.module.auth.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zhubao.manage.common.exception.BusinessException;
import com.zhubao.manage.common.exception.ErrorCode;
import com.zhubao.manage.common.interceptor.UserContextHolder;
import com.zhubao.manage.common.utils.JwtUtil;
import com.zhubao.manage.module.auth.dto.LoginDTO;
import com.zhubao.manage.module.auth.dto.LoginResultDTO;
import com.zhubao.manage.module.role.entity.*;
import com.zhubao.manage.module.role.mapper.*;
import com.zhubao.manage.module.user.entity.User;
import com.zhubao.manage.module.user.mapper.UserMapper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class AuthService {

    private final UserMapper userMapper;
    private final UserRoleMapper userRoleMapper;
    private final RoleMapper roleMapper;
    private final RolePermissionMapper rolePermissionMapper;
    private final PermissionMapper permissionMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final UserContextHolder userContextHolder;

    public AuthService(UserMapper userMapper, UserRoleMapper userRoleMapper,
                       RoleMapper roleMapper, RolePermissionMapper rolePermissionMapper,
                       PermissionMapper permissionMapper, PasswordEncoder passwordEncoder,
                       JwtUtil jwtUtil, UserContextHolder userContextHolder) {
        this.userMapper = userMapper;
        this.userRoleMapper = userRoleMapper;
        this.roleMapper = roleMapper;
        this.rolePermissionMapper = rolePermissionMapper;
        this.permissionMapper = permissionMapper;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.userContextHolder = userContextHolder;
    }

    /**
     * 登录 —— 验证密码 → 返回 JWT + 用户信息 + 角色 + 权限
     */
    @Transactional(readOnly = true)
    public LoginResultDTO login(LoginDTO dto) {
        // 查询用户
        User user = userMapper.selectOne(new LambdaQueryWrapper<User>()
                .eq(User::getUsername, dto.getUsername()));
        if (user == null) {
            throw new BusinessException(ErrorCode.LOGIN_FAILED);
        }

        // 验证密码
        if (!passwordEncoder.matches(dto.getPassword(), user.getPasswordHash())) {
            throw new BusinessException(ErrorCode.LOGIN_FAILED);
        }

        // 检查状态
        if ("DISABLED".equals(user.getStatus())) {
            throw new BusinessException(ErrorCode.ACCOUNT_DISABLED);
        }

        // 更新最后登录时间
        user.setLastLoginAt(LocalDateTime.now());
        userMapper.updateById(user);

        // 生成 JWT —— 包含 storeId/regionId 用于数据权限
        Map<String, Object> claimsMap = new HashMap<>();
        claimsMap.put("userId", user.getId());
        claimsMap.put("username", user.getUsername());
        claimsMap.put("storeId", user.getStoreId());
        claimsMap.put("regionId", user.getRegionId());
        String token = jwtUtil.generateToken(claimsMap);

        // 查询角色和权限
        List<Long> roleIds = userRoleMapper.selectList(
                new LambdaQueryWrapper<UserRole>().eq(UserRole::getUserId, user.getId()))
                .stream().map(UserRole::getRoleId).collect(Collectors.toList());

        List<Role> roles = roleIds.isEmpty() ? Collections.emptyList()
                : roleMapper.selectBatchIds(roleIds);
        List<String> roleCodes = roles.stream().map(Role::getRoleCode).collect(Collectors.toList());

        List<Long> permIds = rolePermissionMapper.selectList(
                new LambdaQueryWrapper<RolePermission>().in(RolePermission::getRoleId, roleIds))
                .stream().map(RolePermission::getPermissionId).distinct().collect(Collectors.toList());

        List<String> permCodes = permIds.isEmpty() ? Collections.emptyList()
                : permissionMapper.selectBatchIds(permIds).stream()
                .map(Permission::getPermCode).collect(Collectors.toList());

        return new LoginResultDTO()
                .token(token)
                .userId(user.getId())
                .username(user.getUsername())
                .realName(user.getRealName())
                .avatar(user.getAvatar())
                .roles(roleCodes)
                .permissions(permCodes);
    }

    /**
     * 获取当前登录用户信息
     */
    @Transactional(readOnly = true)
    public LoginResultDTO getCurrentUser() {
        Long userId = userContextHolder.getUserId();
        if (userId == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }

        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }

        List<Long> roleIds = userRoleMapper.selectList(
                new LambdaQueryWrapper<UserRole>().eq(UserRole::getUserId, userId))
                .stream().map(UserRole::getRoleId).collect(Collectors.toList());

        List<String> roleCodes = roleIds.isEmpty() ? Collections.emptyList()
                : roleMapper.selectBatchIds(roleIds).stream()
                .map(Role::getRoleCode).collect(Collectors.toList());

        return new LoginResultDTO()
                .userId(user.getId())
                .username(user.getUsername())
                .realName(user.getRealName())
                .avatar(user.getAvatar())
                .roles(roleCodes);
    }

    /**
     * 登出（无状态JWT，仅返回成功）
     */
    public void logout() {
        // JWT 无状态，登出仅需前端清除 Token
        // 后续可扩展 Redis 黑名单机制
    }
}
