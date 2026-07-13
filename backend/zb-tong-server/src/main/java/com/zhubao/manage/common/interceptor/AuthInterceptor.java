package com.zhubao.manage.common.interceptor;

import com.zhubao.manage.common.utils.JwtUtil;
import com.zhubao.manage.module.user.entity.User;
import com.zhubao.manage.module.user.mapper.UserMapper;
import io.jsonwebtoken.Claims;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * JWT认证拦截器 —— 校验Token并注入用户上下文（含 storeId/regionId）
 */
@Component
public class AuthInterceptor implements HandlerInterceptor {

    private static final Logger log = LoggerFactory.getLogger(AuthInterceptor.class);

    private final JwtUtil jwtUtil;
    private final UserContextHolder userContextHolder;
    private final UserMapper userMapper;

    public AuthInterceptor(JwtUtil jwtUtil, UserContextHolder userContextHolder,
                           UserMapper userMapper) {
        this.jwtUtil = jwtUtil;
        this.userContextHolder = userContextHolder;
        this.userMapper = userMapper;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String token = extractToken(request);
        if (token == null) {
            log.debug("请求未携带Token: {} {}", request.getMethod(), request.getRequestURI());
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return false;
        }

        try {
            Claims claims = jwtUtil.parseToken(token);
            Long userId = claims.get("userId", Long.class);

            // username: subject 优先，备用 claims.get("username")
            String username = claims.getSubject();
            if (username == null) {
                username = claims.get("username", String.class);
            }

            // storeId/regionId: JWT claims 优先（新Token），null时DB回查（旧Token）
            Long storeId = claims.get("storeId", Long.class);
            Long regionId = claims.get("regionId", Long.class);

            if (storeId == null && regionId == null && userId != null) {
                User user = userMapper.selectById(userId);
                if (user != null) {
                    storeId = user.getStoreId();
                    regionId = user.getRegionId();
                }
            }

            UserContext context = new UserContext();
            context.setUserId(userId);
            context.setUsername(username);
            context.setStoreId(storeId);
            context.setRegionId(regionId);
            userContextHolder.set(context);

            log.debug("认证通过: userId={}, storeId={}, regionId={}, uri={}",
                    userId, storeId, regionId, request.getRequestURI());
            return true;

        } catch (Exception e) {
            log.warn("Token校验失败: {}", e.getMessage());
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return false;
        }
    }

    private String extractToken(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            return header.substring(7);
        }
        return null;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
                                Object handler, Exception ex) {
        userContextHolder.clear();
    }
}
