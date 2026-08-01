package com.zhubao.manage.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Spring Security 配置 —— JWT无状态 + 方法级权限控制
 */
@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf().disable()
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                .authorizeRequests()
                // ---- Knife4j / Swagger ----
                .antMatchers("/doc.html", "/swagger-ui/**", "/swagger-resources/**",
                        "/v2/api-docs/**", "/v3/api-docs/**", "/webjars/**", "/favicon.ico").permitAll()
                // ---- 登录接口 ----
                .antMatchers("/auth/login").permitAll()
                // ---- 静态资源 + 健康检查 ----
                .antMatchers("/static/**", "/public/**", "/actuator/health").permitAll()
                // ---- 其余放行，由 AuthInterceptor(JWT) + @PreAuthorize(角色) 双重校验 ----
                .anyRequest().permitAll()
                .and()
                .formLogin().disable()
                .httpBasic().disable()
                .logout().disable();

        return http.build();
    }
}
