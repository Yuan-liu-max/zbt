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

import javax.servlet.http.Cookie;
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
        // 允许未登录用户浏览商品（GET /products, /categories, /brands）及公开API
        String method = request.getMethod();
        String path = request.getRequestURI();
        if ("GET".equalsIgnoreCase(method) &&
            (path.startsWith("/api/products") || path.startsWith("/api/categories") || path.startsWith("/api/brands"))) {
            return true;
        }
        // 允许公开访问的 POST 端点：登录（注册已关闭，仅管理员可调用）
        if (path.startsWith("/api/shop/auth/login") || path.startsWith("/api/shop/auth/register")
            || path.startsWith("/api/auth/login")) {
            return true;
        }
        // 允许公开访问的 GET 端点：促销、活动、门店
        if ("GET".equalsIgnoreCase(method) &&
            (path.startsWith("/api/promotions") || path.startsWith("/api/activities")
             || path.startsWith("/api/stores"))) {
            return true;
        }

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

            // 每次请求都校验用户状态，防止停用后已有 token 仍可访问
            User user = null;
            if (userId != null) {
                user = userMapper.selectById(userId);
                if (user == null) {
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    return false;
                }
                if ("DISABLED".equals(user.getStatus())) {
                    log.info("用户 {} 已被停用，拒绝访问", user.getUsername());
                    response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                    response.setContentType("application/json;charset=UTF-8");
                    response.getWriter().write("{\"code\":403,\"msg\":\"账号已被停用，请联系管理员\"}");
                    return false;
                }
                // 校验 tokenVersion：管理员强制下线后，旧 token 立即失效
                Integer jwtVersion = claims.get("tokenVersion", Integer.class);
                Integer dbVersion = user.getTokenVersion() != null ? user.getTokenVersion() : 0;
                if (jwtVersion != null && jwtVersion < dbVersion) {
                    log.info("用户 {} 的token已被强制失效 (JWT v{} < DB v{})", user.getUsername(), jwtVersion, dbVersion);
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    response.setContentType("application/json;charset=UTF-8");
                    response.getWriter().write("{\"code\":401,\"msg\":\"账号已被强制下线，请重新登录\"}");
                    return false;
                }
                if (storeId == null) storeId = user.getStoreId();
                if (regionId == null) regionId = user.getRegionId();
            }

            // 优先从 JWT claims 读取 roles（减少DB查询 P1-5），失败时回退到 DB
            String rolesClaim = claims.get("roles", String.class);
            List<String> roleCodes;
            if (rolesClaim != null && !rolesClaim.isEmpty()) {
                roleCodes = java.util.Arrays.asList(rolesClaim.split(","));
            } else {
                roleCodes = resolveRoles(userId);
            }
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

    /** 从角色码推导 data_scope。NONE=顾客角色不注入; ALL=管理员全量; 其他=REGION/STORE/SELF */
    private String resolveDataScopeLevel(List<String> roleCodes) {
        if (roleCodes.isEmpty()) return "SELF";
        try {
            List<Role> roles = roleMapper.selectList(
                    new LambdaQueryWrapper<Role>().in(Role::getRoleCode, roleCodes));
            if (roles.isEmpty()) return "SELF";
            // NONE: 顾客角色，不注入任何数据权限条件
            for (Role r : roles) if ("NONE".equals(r.getDataScope())) return "NONE";
            // ALL: 管理员或总部
            for (Role r : roles) if ("ALL".equals(r.getDataScope())) return "ALL";
            for (Role r : roles) if ("REGION".equals(r.getDataScope())) return "REGION";
            for (Role r : roles) if ("STORE".equals(r.getDataScope())) return "STORE";
            return "SELF";
        } catch (Exception ignored) { return "SELF"; }
    }

    private String extractToken(HttpServletRequest request) {
        String path = request.getRequestURI();

        // 1. 优先从 HttpOnly Cookie 读取 — C端接口用 shop token，M端用 admin token
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            boolean isShopApi = path.startsWith("/api/shop/")
                    || path.startsWith("/api/addresses")
                    || path.startsWith("/api/favorites")
                    || path.startsWith("/api/notifications");
            String primaryCookie = isShopApi ? "zbt_shop_token" : "zbt_admin_token";
            for (Cookie c : cookies) {
                if (primaryCookie.equals(c.getName()) && c.getValue() != null && !c.getValue().isEmpty()) {
                    return c.getValue();
                }
            }
            // 兼容旧版 zbt_token（迁移期过渡）
            for (Cookie c : cookies) {
                if ("zbt_token".equals(c.getName()) && c.getValue() != null && !c.getValue().isEmpty()) {
                    return c.getValue();
                }
            }
        }
        // 2. Authorization Header（各端主动设置，不受 Cookie 串扰影响）
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
