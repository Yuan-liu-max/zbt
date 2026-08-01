package com.zhubao.manage.common.interceptor;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zhubao.manage.common.utils.JwtUtil;
import com.zhubao.manage.module.role.entity.Role;
import com.zhubao.manage.module.role.entity.UserRole;
import com.zhubao.manage.module.role.mapper.RoleMapper;
import com.zhubao.manage.module.role.mapper.UserRoleMapper;
import com.zhubao.manage.module.user.entity.User;
import com.zhubao.manage.module.user.mapper.UserMapper;
import io.jsonwebtoken.Claims;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * JWT认证拦截器 — 校验Token + 注入UserContext + 缓存dataScope + 设置SecurityContext
 */
@Component
public class AuthInterceptor implements HandlerInterceptor {

    private static final Logger log = LoggerFactory.getLogger(AuthInterceptor.class);

    private final JwtUtil jwtUtil;
    private final UserContextHolder userContextHolder;
    private final UserMapper userMapper;
    private final UserRoleMapper userRoleMapper;
    private final RoleMapper roleMapper;

    public AuthInterceptor(JwtUtil jwtUtil, UserContextHolder userContextHolder,
                           UserMapper userMapper, UserRoleMapper urm, RoleMapper rm) {
        this.jwtUtil = jwtUtil;
        this.userContextHolder = userContextHolder;
        this.userMapper = userMapper;
        this.userRoleMapper = urm;
        this.roleMapper = rm;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String token = extractToken(request);
        if (token == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return false;
        }

        try {
            Claims claims = jwtUtil.parseToken(token);
            Long userId = claims.get("userId", Long.class);
            String username = claims.getSubject();
            if (username == null) username = claims.get("username", String.class);

            Long storeId = claims.get("storeId", Long.class);
            Long regionId = claims.get("regionId", Long.class);
            if (storeId == null && regionId == null && userId != null) {
                User user = userMapper.selectById(userId);
                if (user != null) { storeId = user.getStoreId(); regionId = user.getRegionId(); }
            }

            List<String> roleCodes = resolveRoles(userId);
            String dataScopeLevel = resolveDataScopeLevel(roleCodes);

            // Spring Security
            List<SimpleGrantedAuthority> authorities = roleCodes.stream()
                    .map(SimpleGrantedAuthority::new).collect(Collectors.toList());
            SecurityContextHolder.getContext().setAuthentication(
                    new UsernamePasswordAuthenticationToken(userId, null, authorities));

            // UserContext (含 dataScope 缓存)
            UserContext context = new UserContext();
            context.setUserId(userId);
            context.setUsername(username);
            context.setStoreId(storeId);
            context.setRegionId(regionId);
            context.setDataScopeLevel(dataScopeLevel);
            userContextHolder.set(context);

            return true;

        } catch (Exception e) {
            log.warn("Token校验失败: {}", e.getMessage());
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return false;
        }
    }

    private List<String> resolveRoles(Long userId) {
        if (userId == null) return Collections.emptyList();
        try {
            List<Long> roleIds = userRoleMapper.selectList(
                    new LambdaQueryWrapper<UserRole>().eq(UserRole::getUserId, userId))
                    .stream().map(UserRole::getRoleId).collect(Collectors.toList());
            if (roleIds.isEmpty()) return Collections.emptyList();
            return roleMapper.selectBatchIds(roleIds).stream()
                    .map(Role::getRoleCode).collect(Collectors.toList());
        } catch (Exception e) { return Collections.emptyList(); }
    }

    /** 从角色码推导 data_scope: ALL > REGION > STORE > SELF */
    private String resolveDataScopeLevel(List<String> roleCodes) {
        if (roleCodes.isEmpty()) return "ALL";
        // 通过 RoleMapper 查询 dataScope 字段
        try {
            List<Role> roles = roleMapper.selectList(
                    new LambdaQueryWrapper<Role>().in(Role::getRoleCode, roleCodes));
            for (Role r : roles) if ("ALL".equals(r.getDataScope())) return "ALL";
            for (Role r : roles) if ("REGION".equals(r.getDataScope())) return "REGION";
            for (Role r : roles) if ("STORE".equals(r.getDataScope())) return "STORE";
            for (Role r : roles) if ("SELF".equals(r.getDataScope())) return "SELF";
        } catch (Exception ignored) {}
        return "ALL";
    }

    private String extractToken(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) return header.substring(7);
        return null;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
                                Object handler, Exception ex) {
        userContextHolder.clear();
        SecurityContextHolder.clearContext();
    }
}
