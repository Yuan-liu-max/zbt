package com.zhubao.manage.infrastructure.mybatis;

import com.zhubao.manage.common.interceptor.UserContext;
import com.zhubao.manage.common.interceptor.UserContextHolder;
import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.plugin.*;
import org.apache.ibatis.reflection.SystemMetaObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.util.Properties;

/**
 * MyBatis 数据权限拦截插件 —— 从 UserContext 缓存读取 data_scope 级别（避免 DB 连接池耗尽）
 */
@Intercepts({
    @Signature(type = StatementHandler.class, method = "prepare", args = {Connection.class, Integer.class})
})
public class DataScopePlugin implements Interceptor {

    private static final Logger log = LoggerFactory.getLogger(DataScopePlugin.class);

    private final UserContextHolder userContextHolder;

    public DataScopePlugin(UserContextHolder uch) {
        this.userContextHolder = uch;
    }

    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        try {
            UserContext ctx = userContextHolder.get();
            if (ctx == null || ctx.getUserId() == null) return invocation.proceed();

            StatementHandler handler = (StatementHandler) invocation.getTarget();
            BoundSql boundSql = handler.getBoundSql();
            String originalSql = boundSql.getSql().trim();

            // 只处理单表简单 SELECT，跳过复杂查询
            String upper = originalSql.toUpperCase();
            if (!upper.startsWith("SELECT")) return invocation.proceed();
            if (upper.contains(" UNION ") || upper.contains(" JOIN ")
                || upper.contains("COUNT(") || upper.contains("SUM(")
                || upper.contains(" DISTINCT ") || upper.contains("SUBQUERY")) {
                return invocation.proceed();  // 复杂查询跳过不注入
            }

            String level = ctx.getDataScopeLevel();
            if (level == null || "ALL".equals(level) || "NONE".equals(level)) return invocation.proceed();

            String condition = buildCondition(level, ctx);
            if (condition == null || condition.isEmpty()) return invocation.proceed();

            String newSql = injectCondition(originalSql, condition);
            SystemMetaObject.forObject(handler).setValue("delegate.boundSql.sql", newSql);

        } catch (Exception e) {
            log.warn("数据权限注入失败，跳过: {}", e.getMessage());
        }
        return invocation.proceed();
    }

    private String buildCondition(String level, UserContext ctx) {
        switch (level) {
            case "REGION":
                if (ctx.getRegionId() == null) return " AND 1=0 ";
                return " AND (store.region_id = " + ctx.getRegionId() + " OR region_id = " + ctx.getRegionId() + ") ";
            case "STORE":
                if (ctx.getStoreId() == null) return " AND 1=0 ";
                return " AND store_id = " + ctx.getStoreId() + " ";
            case "SELF":
                return " AND user_id = " + ctx.getUserId() + " ";
            default:
                return null;
        }
    }

    private String injectCondition(String sql, String condition) {
        String upper = sql.toUpperCase();
        int pos = upper.lastIndexOf("GROUP BY");
        if (pos < 0) pos = upper.lastIndexOf("ORDER BY");
        if (pos < 0) pos = upper.lastIndexOf("LIMIT");
        if (pos < 0) pos = sql.length();

        if (upper.contains("WHERE")) {
            return sql.substring(0, pos) + condition + " " + sql.substring(pos);
        }
        return sql.substring(0, pos) + " WHERE 1=1 " + condition + " " + sql.substring(pos);
    }

    @Override
    public Object plugin(Object target) {
        return target instanceof StatementHandler ? Plugin.wrap(target, this) : target;
    }

    @Override
    public void setProperties(Properties properties) {}
}
