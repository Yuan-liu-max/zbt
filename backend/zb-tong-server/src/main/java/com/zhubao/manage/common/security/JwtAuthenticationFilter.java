package com.zhubao.manage.common.security;

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
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * JWT 认证过滤器 — 在 Spring Security 链中解析 JWT 并注入 SecurityContext
 */
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    private final JwtUtil jwtUtil;
    private final UserRoleMapper userRoleMapper;
    private final RoleMapper roleMapper;
    private final UserMapper userMapper;

    public JwtAuthenticationFilter(JwtUtil jwtUtil, UserRoleMapper urm, RoleMapper rm, UserMapper um) {
        this.jwtUtil = jwtUtil;
        this.userRoleMapper = urm;
        this.roleMapper = rm;
        this.userMapper = um;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {
        String token = extractToken(request);
        if (token != null) {
            try {
                Claims claims = jwtUtil.parseToken(token);
                Long userId = claims.get("userId", Long.class);

                // 每次请求校验用户状态，防止停用后已有 token 仍可访问
                if (userId != null) {
                    User user = userMapper.selectById(userId);
                    if (user == null) {
                        SecurityContextHolder.clearContext();
                        chain.doFilter(request, response);
                        return;
                    }
                    if ("DISABLED".equals(user.getStatus())) {
                        log.info("用户 {} 已被停用，拒绝访问", user.getUsername());
                        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                        response.setContentType("application/json;charset=UTF-8");
                        response.getWriter().write("{\"code\":403,\"msg\":\"账号已被停用，请联系管理员\"}");
                        return;
                    }
                    // 校验 tokenVersion：管理员强制下线后，旧 token 立即失效
                    Integer jwtVersion = claims.get("tokenVersion", Integer.class);
                    Integer dbVersion = user.getTokenVersion() != null ? user.getTokenVersion() : 0;
                    if (jwtVersion != null && jwtVersion < dbVersion) {
                        log.info("用户 {} 的token已被强制失效 (JWT v{} < DB v{})", user.getUsername(), jwtVersion, dbVersion);
                        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                        response.setContentType("application/json;charset=UTF-8");
                        response.getWriter().write("{\"code\":401,\"msg\":\"账号已被强制下线，请重新登录\"}");
                        return;
                    }
                }

                List<String> roleCodes = resolveRoles(userId);
                List<SimpleGrantedAuthority> authorities = roleCodes.stream()
                        .map(SimpleGrantedAuthority::new).collect(Collectors.toList());

                SecurityContextHolder.getContext().setAuthentication(
                        new UsernamePasswordAuthenticationToken(userId, null, authorities));
            } catch (Exception e) {
                log.debug("JWT解析失败: {}", e.getMessage());
                SecurityContextHolder.clearContext();
            }
        }
        chain.doFilter(request, response);
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

    private String extractToken(HttpServletRequest request) {
        // 1. 优先从 HttpOnly Cookie 读取 — 严格按路径分区，杜绝 admin/shop 互串
        javax.servlet.http.Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            String path = request.getRequestURI();
            // C端接口（/addresses, /favorites, /shop/orders 等）使用 shop token，管理端使用 admin token
            boolean isShopApi = path.startsWith("/api/shop/")
                    || path.startsWith("/api/addresses")
                    || path.startsWith("/api/favorites")
                    || path.startsWith("/api/notifications");
            String primaryCookie = isShopApi ? "zbt_shop_token" : "zbt_admin_token";
            String fallbackCookie = isShopApi ? "zbt_admin_token" : "zbt_shop_token";
            // 优先按路径匹配 cookie
            for (javax.servlet.http.Cookie c : cookies) {
                if (primaryCookie.equals(c.getName()) && c.getValue() != null && !c.getValue().isEmpty()) {
                    return c.getValue();
                }
            }
            // C端接口不接受 admin cookie（防止跨端泄露），M端接口也不接受 shop cookie
            // 兼容旧版 zbt_token
            for (javax.servlet.http.Cookie c : cookies) {
                if ("zbt_token".equals(c.getName()) && c.getValue() != null && !c.getValue().isEmpty()) {
                    return c.getValue();
                }
            }
        }
        // 2. Authorization Header（各端主动设置的 JWT，不受 Cookie 串扰影响）
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) return header.substring(7);
        return null;
    }
}
