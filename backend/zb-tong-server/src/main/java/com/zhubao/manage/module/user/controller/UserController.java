package com.zhubao.manage.module.user.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.zhubao.manage.common.dto.ApiResult;
import com.zhubao.manage.common.dto.PageResult;
import com.zhubao.manage.module.user.dto.UserCreateDTO;
import com.zhubao.manage.module.user.dto.UserQueryDTO;
import com.zhubao.manage.module.user.dto.UserUpdateDTO;
import com.zhubao.manage.module.user.dto.UserVO;
import com.zhubao.manage.module.user.service.UserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@Api(tags = "用户管理")
@RestController
@RequestMapping("/users")
@org.springframework.security.access.prepost.PreAuthorize("hasRole('ADMIN')")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
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
    @PostMapping
    public ApiResult<UserVO> create(@Valid @RequestBody UserCreateDTO dto) {
        return ApiResult.ok(userService.create(dto));
    }

    @ApiOperation("更新用户")
    @PutMapping("/{id}")
    public ApiResult<UserVO> update(@PathVariable Long id, @Valid @RequestBody UserUpdateDTO dto) {
        return ApiResult.ok(userService.update(id, dto));
    }

    @ApiOperation("删除用户")
    @DeleteMapping("/{id}")
    public ApiResult<Void> delete(@PathVariable Long id) {
        userService.delete(id);
        return ApiResult.ok();
    }
}
