package com.zhubao.manage.common.config;

import com.zhubao.manage.common.interceptor.UserContext;
import com.zhubao.manage.common.interceptor.UserContextHolder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskDecorator;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

/**
 * 异步线程池配置 —— 含 TaskDecorator 自动传播 UserContext
 */
@Configuration
@EnableAsync
public class AsyncConfig {

    private final UserContextHolder userContextHolder;

    public AsyncConfig(UserContextHolder uch) {
        this.userContextHolder = uch;
    }

    @Bean("aiExecutor")
    public Executor aiExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(5);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("ai-async-");
        // TaskDecorator: 提交任务时自动复制 UserContext 到子线程
        executor.setTaskDecorator(new UserContextTaskDecorator());
        executor.initialize();
        return executor;
    }

    /**
     * TaskDecorator —— 在 @Async 线程池 submit 时将父线程的 UserContext
     * 复制到子线程，解决 ThreadLocal 跨线程丢失问题。
     */
    private class UserContextTaskDecorator implements TaskDecorator {
        @Override
        public Runnable decorate(Runnable runnable) {
            UserContext parentCtx = userContextHolder.get();
            return () -> {
                try {
                    if (parentCtx != null) {
                        userContextHolder.set(parentCtx);
                    }
                    runnable.run();
                } finally {
                    userContextHolder.clear();
                }
            };
        }
    }
}
