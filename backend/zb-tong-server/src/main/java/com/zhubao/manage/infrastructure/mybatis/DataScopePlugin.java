package com.zhubao.manage.infrastructure.mybatis;

import com.zhubao.manage.common.interceptor.UserContext;
import com.zhubao.manage.common.interceptor.UserContextHolder;
import com.zhubao.manage.module.role.entity.Role;
import com.zhubao.manage.module.role.entity.RoleDataScope;
import com.zhubao.manage.module.role.entity.UserRole;
import com.zhubao.manage.module.role.mapper.RoleDataScopeMapper;
import com.zhubao.manage.module.role.mapper.RoleMapper;
import com.zhubao.manage.module.role.mapper.UserRoleMapper;
import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.plugin.*;
import org.apache.ibatis.reflection.SystemMetaObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.sql.Connection;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

/**
 * MyBatis 数据权限拦截插件 —— 基于用户角色自动拼接 WHERE 条件
 */
@Component
@Intercepts({
    @Signature(type = StatementHandler.class, method = "prepare", args = {Connection.class, Integer.class})
})
public class DataScopePlugin implements Interceptor {

    private static final Logger log = LoggerFactory.getLogger(DataScopePlugin.class);

    private final UserContextHolder userContextHolder;
    private final RoleMapper roleMapper;
    private final UserRoleMapper userRoleMapper;
    private final RoleDataScopeMapper roleDataScopeMapper;

    public DataScopePlugin(UserContextHolder uch, RoleMapper rm,
                           UserRoleMapper urm, RoleDataScopeMapper rdsm) {
        this.userContextHolder = uch;
        this.roleMapper = rm;
        this.userRoleMapper = urm;
        this.roleDataScopeMapper = rdsm;
    }

    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        UserContext ctx = userContextHolder.get();
        if (ctx == null || ctx.getUserId() == null) {
            return invocation.proceed();
        }

        StatementHandler handler = (StatementHandler) invocation.getTarget();
        BoundSql boundSql = handler.getBoundSql();
        String originalSql = boundSql.getSql().trim();

        if (!originalSql.toUpperCase().startsWith("SELECT")) {
            return invocation.proceed();
        }

        // 从数据库查询用户角色 → data_scope 级别
        String dataScopeLevel = getUserDataScopeLevel(ctx.getUserId());
        if ("ALL".equals(dataScopeLevel)) {
            return invocation.proceed();
        }

        String scopeCondition = buildScopeCondition(dataScopeLevel, ctx);
        if (scopeCondition == null || scopeCondition.isEmpty()) {
            return invocation.proceed();
        }

        String newSql = injectCondition(originalSql, scopeCondition);
        SystemMetaObject.forObject(handler).setValue("delegate.boundSql.sql", newSql);
        log.debug("数据权限过滤: userId={}, level={}", ctx.getUserId(), dataScopeLevel);

        return invocation.proceed();
    }

    // ---- 从数据库查真实角色 ----

    private String getUserDataScopeLevel(Long userId) {
        try {
            // sys_user_role → 获取用户的所有角色ID
            List<Long> roleIds = userRoleMapper.selectList(
                    new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<UserRole>()
                            .eq(UserRole::getUserId, userId))
                    .stream().map(UserRole::getRoleId).collect(Collectors.toList());

            // 无角色 → 默认 ALL（不过滤）
            if (roleIds.isEmpty()) return "ALL";

            // sys_role → 获取角色的 data_scope（取最大权限范围）
            List<Role> roles = roleMapper.selectBatchIds(roleIds);
            if (roles.isEmpty()) return "ALL";

            // 权限优先级: ALL > REGION > STORE > SELF > CUSTOM
            for (Role role : roles) {
                if ("ALL".equals(role.getDataScope())) return "ALL";
            }
            for (Role role : roles) {
                if ("REGION".equals(role.getDataScope())) return "REGION";
            }
            for (Role role : roles) {
                if ("STORE".equals(role.getDataScope())) return "STORE";
            }
            for (Role role : roles) {
                if ("SELF".equals(role.getDataScope())) return "SELF";
            }
            // 有角色但 data_scope 为 CUSTOM → 走自定义范围
            return "CUSTOM";
        } catch (Exception e) {
            log.warn("数据权限查询失败，默认 ALL: {}", e.getMessage());
            return "ALL";
        }
    }

    private String buildScopeCondition(String level, UserContext ctx) {
        if (level == null) return null;
        switch (level) {
            case "ALL":
                return null;
            case "REGION":
                if (ctx.getRegionId() == null) return " AND 1=0 ";
                return " AND (store.region_id = " + ctx.getRegionId() + " OR region_id = " + ctx.getRegionId() + ") ";
            case "STORE":
                if (ctx.getStoreId() == null) return " AND 1=0 ";
                return " AND store_id = " + ctx.getStoreId() + " ";
            case "SELF":
                return " AND user_id = " + ctx.getUserId() + " ";
            case "CUSTOM":
                return buildCustomScope(ctx.getUserId());
            default:
                return null;
        }
    }

    private String buildCustomScope(Long userId) {
        try {
            List<Long> roleIds = userRoleMapper.selectList(
                    new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<UserRole>()
                            .eq(UserRole::getUserId, userId))
                    .stream().map(UserRole::getRoleId).collect(Collectors.toList());

            if (roleIds.isEmpty()) return " AND 1=0 ";

            List<RoleDataScope> scopes = roleDataScopeMapper.selectList(
                    new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<RoleDataScope>()
                            .in(RoleDataScope::getRoleId, roleIds));

            if (scopes.isEmpty()) return " AND 1=0 ";

            String storeIds = scopes.stream()
                    .filter(s -> "STORE".equals(s.getScopeType()))
                    .map(s -> String.valueOf(s.getScopeValue()))
                    .collect(Collectors.joining(","));
            if (!storeIds.isEmpty()) return " AND store_id IN (" + storeIds + ") ";

            String regionIds = scopes.stream()
                    .filter(s -> "REGION".equals(s.getScopeType()))
                    .map(s -> String.valueOf(s.getScopeValue()))
                    .collect(Collectors.joining(","));
            if (!regionIds.isEmpty()) return " AND region_id IN (" + regionIds + ") ";

            return " AND 1=0 ";
        } catch (Exception e) {
            log.warn("CUSTOM 数据权限查询失败: {}", e.getMessage());
            return null;
        }
    }

    private String injectCondition(String sql, String condition) {
        String upperSql = sql.toUpperCase();
        if (upperSql.contains("WHERE")) {
            int whereEnd = upperSql.lastIndexOf("GROUP BY");
            if (whereEnd < 0) whereEnd = upperSql.lastIndexOf("ORDER BY");
            if (whereEnd < 0) whereEnd = upperSql.lastIndexOf("LIMIT");
            if (whereEnd < 0) whereEnd = sql.length();
            return sql.substring(0, whereEnd) + condition + " " + sql.substring(whereEnd);
        } else {
            int insertPos = upperSql.lastIndexOf("GROUP BY");
            if (insertPos < 0) insertPos = upperSql.lastIndexOf("ORDER BY");
            if (insertPos < 0) insertPos = upperSql.lastIndexOf("LIMIT");
            if (insertPos < 0) insertPos = sql.length();
            return sql.substring(0, insertPos) + " WHERE 1=1 " + condition + " " + sql.substring(insertPos);
        }
    }

    @Override
    public Object plugin(Object target) {
        return target instanceof StatementHandler ? Plugin.wrap(target, this) : target;
    }

    @Override
    public void setProperties(Properties properties) {}
}
