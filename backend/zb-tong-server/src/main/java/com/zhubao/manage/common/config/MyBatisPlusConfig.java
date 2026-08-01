package com.zhubao.manage.common.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import com.zhubao.manage.common.interceptor.UserContextHolder;
import com.zhubao.manage.infrastructure.mybatis.DataScopePlugin;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * MyBatis-Plus 配置 —— 分页插件 + 逻辑删除 + 数据权限插件
 */
@Configuration
@MapperScan("com.zhubao.manage.module.**.mapper")
public class MyBatisPlusConfig {

    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        PaginationInnerInterceptor paginationInnerInterceptor = new PaginationInnerInterceptor(DbType.MYSQL);
        paginationInnerInterceptor.setMaxLimit(100L);
        paginationInnerInterceptor.setOverflow(false);
        interceptor.addInnerInterceptor(paginationInnerInterceptor);
        return interceptor;
    }

    /**
     * DataScopePlugin —— 显式注册，dataScope 由 AuthInterceptor 预加载到 UserContext
     */
    @Bean
    public DataScopePlugin dataScopePlugin(UserContextHolder uch) {
        return new DataScopePlugin(uch);
    }
}
