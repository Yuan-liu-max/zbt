package com.zhubao.manage.module.user.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.zhubao.manage.common.annotation.OperateLog;
import com.zhubao.manage.common.dto.ApiResult;
import com.zhubao.manage.common.dto.PageResult;
import com.zhubao.manage.common.exception.BusinessException;
import com.zhubao.manage.common.exception.ErrorCode;
import com.zhubao.manage.common.interceptor.UserContextHolder;
import com.zhubao.manage.module.user.dto.UserCreateDTO;
import com.zhubao.manage.module.user.dto.UserQueryDTO;
import com.zhubao.manage.module.user.dto.UserUpdateDTO;
import com.zhubao.manage.module.user.dto.UserVO;
import com.zhubao.manage.module.user.service.UserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Map;

@Api(tags = "用户管理")
@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService userService;
    private final UserContextHolder userContextHolder;

    public UserController(UserService userService, UserContextHolder uch) {
        this.userService = userService; this.userContextHolder = uch;
    }

    @ApiOperation("用户分页列表")
    @GetMapping
    public ApiResult<PageResult<UserVO>> page(@Valid UserQueryDTO query) {
        IPage<UserVO> page = userService.page(query);
        return ApiResult.ok(PageResult.of(page));
    }

    @ApiOperation("用户详情")
    @GetMapping("/{id}")
    public ApiResult<UserVO> detail(@PathVariable Long id) {
        return ApiResult.ok(userService.detail(id));
    }

    @ApiOperation("新增用户")
    @OperateLog(module = "用户管理", action = "CREATE", targetType = "USER")
    @PostMapping
    @org.springframework.security.access.prepost.PreAuthorize("hasRole('ADMIN')")
    public ApiResult<UserVO> create(@Valid @RequestBody UserCreateDTO dto) {
        return ApiResult.ok(userService.create(dto));
    }

    @ApiOperation("更新用户")
    @OperateLog(module = "用户管理", action = "UPDATE", targetType = "USER", targetIdExpr = "#id")
    @PutMapping("/{id}")
    @org.springframework.security.access.prepost.PreAuthorize("hasRole('ADMIN')")
    public ApiResult<UserVO> update(@PathVariable Long id, @Valid @RequestBody UserUpdateDTO dto) {
        return ApiResult.ok(userService.update(id, dto));
    }

    @ApiOperation("删除用户")
    @OperateLog(module = "用户管理", action = "DELETE", targetType = "USER", targetIdExpr = "#id")
    @DeleteMapping("/{id}")
    @org.springframework.security.access.prepost.PreAuthorize("hasRole('ADMIN')")
    public ApiResult<Void> delete(@PathVariable Long id) {
        userService.delete(id);
        return ApiResult.ok();
    }

    @ApiOperation("强制下线")
    @OperateLog(module = "用户管理", action = "FORCE_LOGOUT", targetType = "USER", targetIdExpr = "#id")
    @PutMapping("/{id}/force-logout")
    @org.springframework.security.access.prepost.PreAuthorize("hasRole('ADMIN')")
    public ApiResult<Void> forceLogout(@PathVariable Long id) {
        userService.forceLogout(id);
        return ApiResult.ok();
    }

    @ApiOperation("修改密码")
    @PutMapping("/change-password")
    public ApiResult<Void> changePassword(@RequestBody Map<String, String> body) {
        Long userId = userContextHolder.getUserId();
        if (userId == null) throw new BusinessException(ErrorCode.UNAUTHORIZED);
        userService.changePassword(userId, body.get("currentPassword"), body.get("newPassword"));
        return ApiResult.ok();
    }
}
