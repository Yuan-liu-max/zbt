package com.zhubao.manage.common.interceptor;

import com.zhubao.manage.common.utils.JwtUtil;
import io.jsonwebtoken.Claims;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * JWT认证拦截器 —— 校验Token并注入用户上下文
 */
@Component
public class AuthInterceptor implements HandlerInterceptor {

    private static final Logger log = LoggerFactory.getLogger(AuthInterceptor.class);

    private final JwtUtil jwtUtil;
    private final UserContextHolder userContextHolder;

    public AuthInterceptor(JwtUtil jwtUtil, UserContextHolder userContextHolder) {
        this.jwtUtil = jwtUtil;
        this.userContextHolder = userContextHolder;
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
            String username = claims.getSubject();

            // 注入用户上下文
            UserContext context = new UserContext();
            context.setUserId(userId);
            context.setUsername(username);
            userContextHolder.set(context);

            log.debug("认证通过: userId={}, username={}, uri={}", userId, username, request.getRequestURI());
            return true;

        } catch (Exception e) {
            log.warn("Token校验失败: {}", e.getMessage());
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return false;
        }
    }

    /**
     * 从请求头提取 Bearer Token
     */
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
        // 请求结束后清理上下文，防止内存泄漏
        userContextHolder.clear();
    }
}
