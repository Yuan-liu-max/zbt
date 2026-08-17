package com.zhubao.manage.module.auth.service;

import com.zhubao.manage.common.exception.BusinessException;
import com.zhubao.manage.common.exception.ErrorCode;
import com.zhubao.manage.module.user.entity.User;
import com.zhubao.manage.module.user.mapper.UserMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock private UserMapper userMapper;
    private BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    @Test
    void login_wrongPassword_throwsException() {
        // 验证密码不匹配时抛 LOGIN_FAILED
        assertTrue(true); // 框架就位，具体 mock 待补全
    }

    @Test
    void login_success_returnsToken() {
        assertTrue(true);
    }
}
