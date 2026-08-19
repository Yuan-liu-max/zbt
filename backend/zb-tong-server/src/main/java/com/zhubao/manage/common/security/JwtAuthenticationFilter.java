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
import java.util.ArrayList;
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
        List<String> candidates = extractTokenCandidates(request);
        boolean authenticated = false;
        for (String token : candidates) {
            try {
                Claims claims = jwtUtil.parseToken(token);
                Long userId = claims.get("userId", Long.class);

                // 每次请求校验用户状态，防止停用后已有 token 仍可访问
                if (userId != null) {
                    User user = userMapper.selectById(userId);
                    if (user == null) {
                        continue;
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
                        continue;
                    }
                }

                List<String> roleCodes = resolveRoles(userId);
                List<SimpleGrantedAuthority> authorities = roleCodes.stream()
                        .map(SimpleGrantedAuthority::new).collect(Collectors.toList());

                SecurityContextHolder.getContext().setAuthentication(
                        new UsernamePasswordAuthenticationToken(userId, null, authorities));
                authenticated = true;
                break;
            } catch (Exception e) {
                log.debug("JWT解析失败: {}", e.getMessage());
                SecurityContextHolder.clearContext();
            }
        }
        if (!authenticated) {
            SecurityContextHolder.clearContext();
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

    /**
     * 收集所有候选 token（按优先级排序）：
     * 1. 路径分区的 primary Cookie（shop 接口→shop token，其余→admin token）
     * 2. 共用接口回退 Cookie（非 C 端专属接口回退 shop token）
     * 3. 兼容旧版 zbt_token
     * 4. Authorization Header
     * 调用方逐个尝试解析，第一个解析成功的生效（避免失效 admin cookie 阻塞有效 shop cookie）
     */
    private List<String> extractTokenCandidates(HttpServletRequest request) {
        List<String> candidates = new ArrayList<>();
        String path = request.getRequestURI();

        // 1. Cookie 候选
        javax.servlet.http.Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            // C端接口（/addresses, /favorites, /shop/orders 等）使用 shop token，管理端使用 admin token
            boolean isShopApi = path.startsWith("/api/shop/")
                    || path.startsWith("/api/addresses")
                    || path.startsWith("/api/favorites")
                    || path.startsWith("/api/notifications");
            String primaryCookie = isShopApi ? "zbt_shop_token" : "zbt_admin_token";
            // 非 C 端专属接口（AI、文件上传等双端共用接口）：主 Cookie 缺失或失效时回退 shop Cookie
            String fallbackCookie = isShopApi ? null : "zbt_shop_token";

            // 按优先级收集 cookie 值（去重）
            addCookieValue(candidates, cookies, primaryCookie);
            if (fallbackCookie != null) addCookieValue(candidates, cookies, fallbackCookie);
            addCookieValue(candidates, cookies, "zbt_token");
        }

        // 2. Authorization Header（各端主动设置的 JWT，不受 Cookie 串扰影响）
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            candidates.add(header.substring(7));
        }
        return candidates;
    }

    private void addCookieValue(List<String> candidates, javax.servlet.http.Cookie[] cookies, String name) {
        for (javax.servlet.http.Cookie c : cookies) {
            if (name.equals(c.getName()) && c.getValue() != null && !c.getValue().isEmpty()) {
                if (!candidates.contains(c.getValue())) {
                    candidates.add(c.getValue());
                }
                break;
            }
        }
    }
}
