package com.zhubao.manage.infrastructure.aop;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zhubao.manage.common.annotation.OperateLog;
import com.zhubao.manage.common.interceptor.UserContext;
import com.zhubao.manage.common.interceptor.UserContextHolder;
import com.zhubao.manage.module.report.mapper.OperateLogMapper;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;
import java.util.*;

/**
 * 操作日志 AOP 切面
 *
 * 拦截 @OperateLog 注解的 Controller 方法
 * 异步写入 operate_log 表
 */
@Aspect
@Component
public class OperateLogAspect {

    private static final Logger log = LoggerFactory.getLogger(OperateLogAspect.class);
    private static final ObjectMapper mapper = new ObjectMapper();

    private final OperateLogMapper operateLogMapper;
    private final UserContextHolder userContextHolder;

    public OperateLogAspect(OperateLogMapper olm, UserContextHolder uch) {
        this.operateLogMapper = olm;
        this.userContextHolder = uch;
    }

    @Around("@annotation(com.zhubao.manage.common.annotation.OperateLog)")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        long start = System.currentTimeMillis();
        Object result = joinPoint.proceed();
        long elapsed = System.currentTimeMillis() - start;

        // 异步记录
        try {
            asyncLog(joinPoint, result);
        } catch (Exception e) {
            log.warn("操作日志记录失败", e);
        }

        return result;
    }

    @Async("aiExecutor")
    public void asyncLog(ProceedingJoinPoint joinPoint, Object result) {
        try {
            // 保存 ThreadLocal 上下文，避免异步丢失 (P2-19 fix)
            UserContext ctx = userContextHolder.get();
            Long operatorId = ctx != null ? ctx.getUserId() : null;

            MethodSignature signature = (MethodSignature) joinPoint.getSignature();
            Method method = signature.getMethod();
            OperateLog annotation = method.getAnnotation(OperateLog.class);

            com.zhubao.manage.module.report.entity.OperateLog entity =
                    new com.zhubao.manage.module.report.entity.OperateLog();

            entity.setOperatorId(operatorId);

            // 注解信息
            entity.setModule(annotation.module());
            entity.setAction(annotation.action());
            entity.setTargetType(annotation.targetType());

            // 目标ID（从 SpEL 或方法参数解析）
            Long targetId = resolveTargetId(annotation.targetIdExpr(), joinPoint.getArgs(),
                    signature.getParameterNames());
            entity.setTargetId(targetId);

            // 请求IP
            try {
                HttpServletRequest request = ((ServletRequestAttributes)
                        RequestContextHolder.currentRequestAttributes()).getRequest();
                entity.setRequestIp(getClientIp(request));
            } catch (Exception ignored) {}

            // 请求参数（脱敏）
            try {
                entity.setRequestParams(sanitize(joinPoint.getArgs()));
            } catch (Exception ignored) {}

            // 结果摘要
            try {
                entity.setNewData(result != null ? mapper.writeValueAsString(result).substring(0,
                        Math.min(500, mapper.writeValueAsString(result).length())) : null);
            } catch (Exception ignored) {}

            operateLogMapper.insert(entity);
        } catch (Exception e) {
            log.error("异步日志写入异常", e);
        }
    }

    /** 解析 SpEL 或简单参数名 */
    private Long resolveTargetId(String expr, Object[] args, String[] paramNames) {
        if (expr == null || expr.isEmpty()) {
            // 自动搜索名为 id 的参数
            if (paramNames != null) {
                for (int i = 0; i < paramNames.length; i++) {
                    if ("id".equals(paramNames[i]) && args[i] instanceof Long) {
                        return (Long) args[i];
                    }
                }
            }
            return null;
        }
        // 简单处理：#id → 按参数名查找
        String name = expr.replace("#", "").trim();
        if (paramNames != null) {
            for (int i = 0; i < paramNames.length; i++) {
                if (name.equals(paramNames[i]) && args[i] instanceof Long) {
                    return (Long) args[i];
                }
            }
        }
        return null;
    }

    /** 脱敏：密码/手机号/Token等敏感字段替换为 *** (P2-19 fix) */
    private String sanitize(Object[] args) {
        try {
            String json = mapper.writeValueAsString(args);
            // 敏感字段脱敏
            json = json.replaceAll("\"password\"\\s*:\\s*\"[^\"]*\"", "\"password\":\"***\"");
            json = json.replaceAll("\"passwordHash\"\\s*:\\s*\"[^\"]*\"", "\"passwordHash\":\"***\"");
            json = json.replaceAll("\"phone\"\\s*:\\s*\"[^\"]*\"", "\"phone\":\"***\"");
            json = json.replaceAll("\"token\"\\s*:\\s*\"[^\"]*\"", "\"token\":\"***\"");
            json = json.replaceAll("\"secret\"\\s*:\\s*\"[^\"]*\"", "\"secret\":\"***\"");
            return json.length() > 2000 ? json.substring(0, 2000) : json;
        } catch (Exception e) {
            return Arrays.toString(args);
        }
    }

    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty()) ip = request.getHeader("X-Real-IP");
        if (ip == null || ip.isEmpty()) ip = request.getRemoteAddr();
        return ip;
    }
}
