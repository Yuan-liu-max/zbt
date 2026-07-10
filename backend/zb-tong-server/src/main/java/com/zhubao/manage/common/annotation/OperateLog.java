package com.zhubao.manage.common.annotation;

import java.lang.annotation.*;

/**
 * 操作日志注解 —— 标记需要记录操作日志的 Controller 方法
 *
 * <pre>
 * 使用示例:
 *   {@code @OperateLog(module = "用户管理", action = "CREATE", targetType = "USER")}
 *   public ApiResult createUser(@RequestBody UserCreateDTO dto) { ... }
 * </pre>
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface OperateLog {

    /** 操作模块 */
    String module();

    /** 操作动作: CREATE/UPDATE/DELETE/SUBMIT/AUDIT/LOGIN/EXPORT */
    String action();

    /** 操作对象类型: USER/ROLE/TASK/PRODUCT/STORE/... */
    String targetType() default "";

    /** 目标ID的SpEL表达式，如 #id / #dto.userId */
    String targetIdExpr() default "";
}
