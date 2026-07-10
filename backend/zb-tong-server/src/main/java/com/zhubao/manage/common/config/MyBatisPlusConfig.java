package com.zhubao.manage.common.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * MyBatis-Plus 配置 —— 分页插件 + 逻辑删除
 *
 * 逻辑删除配置在 application.yml 中:
 *   mybatis-plus.global-config.db-config.logic-delete-field=isDeleted
 *   mybatis-plus.global-config.db-config.logic-delete-value=1
 *   mybatis-plus.global-config.db-config.logic-not-delete-value=0
 */
@Configuration
@MapperScan("com.zhubao.manage.module.**.mapper")
public class MyBatisPlusConfig {

    /**
     * MyBatis-Plus 拦截器（分页插件）
     */
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        PaginationInnerInterceptor paginationInnerInterceptor = new PaginationInnerInterceptor(DbType.MYSQL);
        paginationInnerInterceptor.setMaxLimit(100L);
        paginationInnerInterceptor.setOverflow(false);
        interceptor.addInnerInterceptor(paginationInnerInterceptor);
        return interceptor;
    }
}
