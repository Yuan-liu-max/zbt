package com.zhubao.manage.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Spring Security 配置 —— 放行登录/Knife4j/静态资源，其余通过拦截器校验JWT
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // 禁用 CSRF（前后端分离，Token模式）
                .csrf().disable()

                // 无状态Session
                .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()

                // 请求权限配置
                .authorizeRequests()
                // ---- Knife4j / Swagger 文档 ----
                .antMatchers(
                        "/doc.html",
                        "/swagger-ui/**",
                        "/swagger-resources/**",
                        "/v2/api-docs/**",
                        "/v3/api-docs/**",
                        "/webjars/**",
                        "/favicon.ico"
                ).permitAll()
                // ---- 登录接口放行 ----
                .antMatchers("/auth/login", "/auth/logout").permitAll()
                // ---- 静态资源 ----
                .antMatchers("/static/**", "/public/**").permitAll()
                // ---- 健康检查 ----
                .antMatchers("/actuator/health").permitAll()
                // ---- 其余全部需要认证 ----
                .anyRequest().authenticated()
                .and()

                // 禁用默认表单登录/HTTP Basic
                .formLogin().disable()
                .httpBasic().disable()

                // 禁用默认登出
                .logout().disable();

        return http.build();
    }
}
