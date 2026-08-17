package com.zhubao.manage.module.auth.controller;

import com.zhubao.manage.common.dto.ApiResult;
import com.zhubao.manage.common.exception.BusinessException;
import com.zhubao.manage.common.interceptor.UserContextHolder;
import com.zhubao.manage.common.security.LoginRateLimiter;
import com.zhubao.manage.module.auth.dto.LoginDTO;
import com.zhubao.manage.module.auth.dto.LoginResultDTO;
import com.zhubao.manage.module.auth.service.AuthService;
import com.zhubao.manage.module.user.entity.User;
import com.zhubao.manage.module.user.mapper.UserMapper;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.util.*;

@Api(tags = "认证管理")
@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;
    private final UserMapper userMapper;
    private final UserContextHolder userContextHolder;
    private final PasswordEncoder passwordEncoder;
    private final LoginRateLimiter loginRateLimiter;

    @Value("${app.cookie-secure:false}")
    private boolean cookieSecure;

    public AuthController(AuthService authService, UserMapper um,
                          UserContextHolder uch, PasswordEncoder pe,
                          LoginRateLimiter loginRateLimiter) {
        this.authService = authService;
        this.userMapper = um;
        this.userContextHolder = uch;
        this.passwordEncoder = pe;
        this.loginRateLimiter = loginRateLimiter;
    }

    @ApiOperation("用户登录")
    @PostMapping("/login")
    public ApiResult<LoginResultDTO> login(@Valid @RequestBody LoginDTO dto, HttpServletResponse response) {
        String key = "admin:" + (dto.getUsername() != null ? dto.getUsername().trim() : "");
        if (loginRateLimiter.isLocked(key)) {
            return ApiResult.fail(429, "登录失败次数过多，请15分钟后再试");
        }
        try {
            LoginResultDTO result = authService.login(dto);
            loginRateLimiter.onSuccess(key);
            setTokenCookie(response, result.getToken());
            return ApiResult.ok(result);
        } catch (BusinessException e) {
            loginRateLimiter.onFailure(key);
            throw e;
        }
    }

    @ApiOperation("用户注册（默认导购角色，仅管理员可用）")
    @PreAuthorize("hasAnyRole('ADMIN','HQ')")
    @PostMapping("/register")
    public ApiResult<LoginResultDTO> register(@RequestBody Map<String, String> body, HttpServletResponse response) {
        LoginResultDTO result = authService.register(body.get("username"), body.get("password"), body.get("phone"));
        setTokenCookie(response, result.getToken());
        return ApiResult.ok(result);
    }

    @PreAuthorize("isAuthenticated()")
    @ApiOperation("获取当前用户信息")
    @GetMapping("/me")
    public ApiResult<LoginResultDTO> me() {
        return ApiResult.ok(authService.getCurrentUser());
    }

    @PreAuthorize("isAuthenticated()")
    @ApiOperation("用户登出")
    @PostMapping("/logout")
    public ApiResult<Void> logout(HttpServletResponse response) {
        authService.logout();
        clearTokenCookie(response);
        return ApiResult.ok();
    }

    // ---- Cookie 工具 ----

    /** 设置管理端 Cookie（不主动清除商城端，允许双端共存；AuthInterceptor 按路径分区选取） */
    private void setTokenCookie(HttpServletResponse response, String token) {
        String secure = cookieSecure ? "; Secure" : "";
        response.addHeader("Set-Cookie", String.format(
            "zbt_admin_token=%s; HttpOnly%s; Path=/; Max-Age=259200; SameSite=Lax", token, secure));
    }

    private void clearTokenCookie(HttpServletResponse response) {
        String secure = cookieSecure ? "; Secure" : "";
        response.addHeader("Set-Cookie",
            "zbt_admin_token=; HttpOnly" + secure + "; Path=/; Max-Age=0; SameSite=Lax");
    }

    @PreAuthorize("isAuthenticated()")
    @ApiOperation("用户统计")
    @GetMapping("/stats")
    public ApiResult<Map<String, Object>> stats() {
        return ApiResult.ok(authService.getUserStats());
    }

    @PreAuthorize("isAuthenticated()")
    @ApiOperation("更新个人信息")
    @PutMapping("/profile")
    public ApiResult<Void> updateProfile(@RequestBody Map<String, Object> body) {
        Long userId = userContextHolder.getUserId();
        User user = userMapper.selectById(userId);
        if (user == null) return ApiResult.fail("用户不存在");
        if (body.containsKey("realName")) user.setRealName(asString(body.get("realName")));
        if (body.containsKey("phone")) user.setPhone(asString(body.get("phone")));
        if (body.containsKey("email")) user.setEmail(asString(body.get("email")));
        if (body.containsKey("avatar")) user.setAvatar(asString(body.get("avatar")));
        if (body.containsKey("timezone")) user.setTimezone(asString(body.get("timezone")));
        if (body.containsKey("language")) user.setLanguage(asString(body.get("language")));
        if (body.containsKey("dateFormat")) user.setDateFormat(asString(body.get("dateFormat")));
        if (body.containsKey("notifySystem")) user.setNotifySystem(asBool(body.get("notifySystem")));
        if (body.containsKey("notifyOrder")) user.setNotifyOrder(asBool(body.get("notifyOrder")));
        if (body.containsKey("notifyInventory")) user.setNotifyInventory(asBool(body.get("notifyInventory")));
        if (body.containsKey("notifyMarketing")) user.setNotifyMarketing(asBool(body.get("notifyMarketing")));
        userMapper.updateById(user);
        return ApiResult.ok();
    }

    private String asString(Object v) {
        return v == null ? null : String.valueOf(v);
    }

    private Boolean asBool(Object v) {
        if (v == null) return null;
        if (v instanceof Boolean) return (Boolean) v;
        return "true".equalsIgnoreCase(String.valueOf(v)) || "1".equals(String.valueOf(v));
    }

    @PreAuthorize("isAuthenticated()")
    @ApiOperation("修改密码")
    @PutMapping("/password")
    public ApiResult<Void> changePassword(@RequestBody Map<String, String> body) {
        Long userId = userContextHolder.getUserId();
        User user = userMapper.selectById(userId);
        if (user == null) return ApiResult.fail("用户不存在");
        String oldPwd = body.get("oldPassword");
        String newPwd = body.get("newPassword");
        if (oldPwd == null || newPwd == null) return ApiResult.fail("参数不全");
        if (!passwordEncoder.matches(oldPwd, user.getPasswordHash())) {
            return ApiResult.fail("原密码错误");
        }
        user.setPasswordHash(passwordEncoder.encode(newPwd));
        userMapper.updateById(user);
        return ApiResult.ok();
    }

    @PreAuthorize("isAuthenticated()")
    @ApiOperation("注销账号（软禁用）")
    @DeleteMapping("/account")
    public ApiResult<Void> deactivate() {
        Long userId = userContextHolder.getUserId();
        User user = userMapper.selectById(userId);
        if (user == null) return ApiResult.fail("用户不存在");
        user.setStatus("DISABLED");
        userMapper.updateById(user);
        return ApiResult.ok();
    }
}
