package com.zhubao.manage.common.interceptor;

import org.springframework.stereotype.Component;

/**
 * 用户上下文持有者 —— ThreadLocal 存储当前请求的用户信息
 */
@Component
public class UserContextHolder {

    private static final ThreadLocal<UserContext> CONTEXT = new ThreadLocal<>();

    /**
     * 设置当前线程的用户上下文
     */
    public void set(UserContext context) {
        CONTEXT.set(context);
    }

    /**
     * 获取当前线程的用户上下文
     */
    public UserContext get() {
        return CONTEXT.get();
    }

    /**
     * 获取当前用户ID
     */
    public Long getUserId() {
        UserContext ctx = CONTEXT.get();
        return ctx != null ? ctx.getUserId() : null;
    }

    /**
     * 获取当前用户名
     */
    public String getUsername() {
        UserContext ctx = CONTEXT.get();
        return ctx != null ? ctx.getUsername() : null;
    }

    /**
     * 清除当前线程的用户上下文
     */
    public void clear() {
        CONTEXT.remove();
    }
}
