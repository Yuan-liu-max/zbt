package com.zhubao.manage.common.security;

import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;

/**
 * 登录失败限流 —— 内存版（单实例）
 *
 * 连续失败 {@value #MAX_ATTEMPTS} 次锁定 {@value #LOCK_MINUTES} 分钟，登录成功清零。
 * 注：生产多实例部署时建议替换为 Redis 实现（跨实例共享计数）。
 */
@Component
public class LoginRateLimiter {

    private static final int MAX_ATTEMPTS = 5;
    private static final long LOCK_MS = 15 * 60 * 1000L;

    private static class Record {
        int failures;
        long lockedUntil;
    }

    private final ConcurrentHashMap<String, Record> map = new ConcurrentHashMap<>();

    /** 是否处于锁定状态（过期自动清除） */
    public boolean isLocked(String key) {
        Record r = map.get(key);
        if (r == null) return false;
        if (r.lockedUntil > 0) {
            if (System.currentTimeMillis() < r.lockedUntil) return true;
            map.remove(key); // 锁已过期
        }
        return false;
    }

    /** 记录一次失败，达到阈值则锁定 */
    public void onFailure(String key) {
        Record r = map.computeIfAbsent(key, k -> new Record());
        synchronized (r) {
            r.failures++;
            if (r.failures >= MAX_ATTEMPTS) {
                r.lockedUntil = System.currentTimeMillis() + LOCK_MS;
                r.failures = 0;
            }
        }
    }

    /** 登录成功清零 */
    public void onSuccess(String key) {
        map.remove(key);
    }
}
