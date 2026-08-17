package com.zhubao.manage.common.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zhubao.manage.common.security.JwtAuthenticationFilter;
import com.zhubao.manage.common.utils.JwtUtil;
import com.zhubao.manage.module.role.mapper.RoleMapper;
import com.zhubao.manage.module.role.mapper.UserRoleMapper;
import com.zhubao.manage.module.user.mapper.UserMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Spring Security 配置 — JWT Filter 在 Security 链内解析 Token + 注入 SecurityContext
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
    public JwtAuthenticationFilter jwtAuthenticationFilter(JwtUtil jwtUtil,
                                                           UserRoleMapper urm, RoleMapper rm,
                                                           UserMapper um) {
        return new JwtAuthenticationFilter(jwtUtil, urm, rm, um);
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, JwtAuthenticationFilter jwtFilter) throws Exception {
        http
                .csrf().disable()
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
                .exceptionHandling()
                .authenticationEntryPoint((req, res, e) -> {
                    res.setStatus(HttpStatus.UNAUTHORIZED.value());
                    res.setContentType("application/json;charset=UTF-8");
                    java.util.Map<String, Object> m = new java.util.HashMap<>();
                    m.put("code", 401); m.put("msg", "未登录或Token已过期");
                    res.getWriter().write(new ObjectMapper().writeValueAsString(m));
                })
                .accessDeniedHandler((req, res, e) -> {
                    res.setStatus(HttpStatus.FORBIDDEN.value());
                    res.setContentType("application/json;charset=UTF-8");
                    java.util.Map<String, Object> m = new java.util.HashMap<>();
                    m.put("code", 403); m.put("msg", "无权限");
                    res.getWriter().write(new ObjectMapper().writeValueAsString(m));
                })
                .and()
                .authorizeRequests()
                .antMatchers("/doc.html", "/swagger-ui/**", "/swagger-resources/**",
                        "/v2/api-docs/**", "/v3/api-docs/**", "/webjars/**", "/favicon.ico").permitAll()
                .antMatchers("/auth/login").permitAll()
                .antMatchers("/shop/auth/**").permitAll()
                .antMatchers(HttpMethod.GET, "/products", "/products/**", "/categories/**", "/brands/**").permitAll()
                .antMatchers(HttpMethod.GET, "/products/categories/tree", "/products/brands/all").permitAll()
                .antMatchers(HttpMethod.GET, "/promotions/**", "/activities/**", "/stores/**").permitAll()
                .antMatchers("/static/**", "/public/**", "/files/static/**", "/images/**", "/actuator/health").permitAll()
                .antMatchers("/shop/cart/**", "/addresses/**", "/shop/orders/**", "/favorites/**").authenticated()
                .anyRequest().authenticated()
                .and()
                .formLogin().disable()
                .httpBasic().disable()
                .logout().disable();

        return http.build();
    }
}
