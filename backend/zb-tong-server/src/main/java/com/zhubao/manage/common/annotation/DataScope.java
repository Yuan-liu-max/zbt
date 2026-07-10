package com.zhubao.manage.common.annotation;

import java.lang.annotation.*;

/**
 * 数据权限注解 —— 标记需要数据过滤的 Mapper 方法
 *
 * <pre>
 * 使用示例:
 *   {@code @DataScope(storeAlias = "t", userAlias = "t")}
 *   List&lt;TaskInstance&gt; selectPage(IPage page, @Param("ew") Wrapper wrapper);
 * </pre>
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
public @interface DataScope {

    /** 门店字段的 SQL 别名，默认空表示使用字段原名 store_id */
    String storeAlias() default "";

    /** 用户字段的 SQL 别名，默认空表示使用字段原名 user_id */
    String userAlias() default "";
}
