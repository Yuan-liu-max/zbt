package com.zhubao.manage.module.auth.controller;

import com.zhubao.manage.common.dto.ApiResult;
import com.zhubao.manage.module.auth.dto.LoginDTO;
import com.zhubao.manage.module.auth.dto.LoginResultDTO;
import com.zhubao.manage.module.auth.service.AuthService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import org.springframework.security.access.prepost.PreAuthorize;
import javax.validation.Valid;

@Api(tags = "认证管理")
@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
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
}
