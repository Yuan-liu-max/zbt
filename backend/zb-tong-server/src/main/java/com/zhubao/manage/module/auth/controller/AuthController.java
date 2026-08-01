package com.zhubao.manage.module.auth.controller;

import com.zhubao.manage.common.dto.ApiResult;
import com.zhubao.manage.common.exception.BusinessException;
import com.zhubao.manage.common.interceptor.UserContextHolder;
import com.zhubao.manage.module.auth.dto.LoginDTO;
import com.zhubao.manage.module.auth.dto.LoginResultDTO;
import com.zhubao.manage.module.auth.service.AuthService;
import com.zhubao.manage.module.user.entity.User;
import com.zhubao.manage.module.user.mapper.UserMapper;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

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

    public AuthController(AuthService authService, UserMapper um,
                          UserContextHolder uch, PasswordEncoder pe) {
        this.authService = authService;
        this.userMapper = um;
        this.userContextHolder = uch;
        this.passwordEncoder = pe;
    }

    @ApiOperation("用户登录")
    @PostMapping("/login")
    public ApiResult<LoginResultDTO> login(@Valid @RequestBody LoginDTO dto) {
        return ApiResult.ok(authService.login(dto));
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
    public ApiResult<Void> logout() {
        authService.logout();
        return ApiResult.ok();
    }

    @PreAuthorize("isAuthenticated()")
    @ApiOperation("用户统计")
    @GetMapping("/stats")
    public ApiResult<Map<String, Object>> stats() {
        Map<String, Object> s = new LinkedHashMap<>();
        s.put("productCount", 0); s.put("orderCount", 0);
        s.put("todayVisitors", 0); s.put("todaySales", 0);
        return ApiResult.ok(s);
    }

    @PreAuthorize("isAuthenticated()")
    @ApiOperation("更新个人信息")
    @PutMapping("/profile")
    public ApiResult<Void> updateProfile(@RequestBody Map<String, Object> body) {
        Long userId = userContextHolder.getUserId();
        User user = userMapper.selectById(userId);
        if (user == null) return ApiResult.fail("用户不存在");
        if (body.containsKey("phone")) user.setPhone((String) body.get("phone"));
        if (body.containsKey("avatar")) user.setAvatar((String) body.get("avatar"));
        userMapper.updateById(user);
        return ApiResult.ok();
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
}
