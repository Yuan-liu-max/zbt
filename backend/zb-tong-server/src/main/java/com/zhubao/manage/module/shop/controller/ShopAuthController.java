package com.zhubao.manage.module.shop.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zhubao.manage.common.dto.ApiResult;
import com.zhubao.manage.common.exception.BusinessException;
import com.zhubao.manage.common.exception.ErrorCode;
import com.zhubao.manage.common.interceptor.UserContextHolder;
import com.zhubao.manage.common.security.LoginRateLimiter;
import com.zhubao.manage.common.utils.JwtUtil;
import com.zhubao.manage.module.auth.dto.LoginResultDTO;
import com.zhubao.manage.module.customer.entity.Customer;
import com.zhubao.manage.module.customer.mapper.CustomerMapper;
import com.zhubao.manage.module.role.entity.Role;
import com.zhubao.manage.module.role.entity.UserRole;
import com.zhubao.manage.module.role.mapper.RoleMapper;
import com.zhubao.manage.module.role.mapper.UserRoleMapper;
import com.zhubao.manage.module.user.entity.User;
import com.zhubao.manage.module.user.mapper.UserMapper;
import com.zhubao.manage.module.order.mapper.OrderMapper;
import com.zhubao.manage.module.order.entity.Order;
import com.zhubao.manage.module.shop.mapper.UserFavoriteMapper;
import com.zhubao.manage.module.shop.entity.UserFavorite;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * C端商城认证控制器 —— 仅限 ROLE_CUSTOMER 用户登录
 */
@Api(tags = "商城认证")
@RestController
@RequestMapping("/shop/auth")
public class ShopAuthController {

    private final UserMapper userMapper;
    private final UserRoleMapper userRoleMapper;
    private final RoleMapper roleMapper;
    private final CustomerMapper customerMapper;
    private final OrderMapper orderMapper;
    private final UserFavoriteMapper userFavoriteMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final UserContextHolder userContextHolder;
    private final LoginRateLimiter loginRateLimiter;

    @Value("${app.cookie-secure:false}")
    private boolean cookieSecure;

    public ShopAuthController(UserMapper um, UserRoleMapper urm, RoleMapper rm,
                              CustomerMapper cm,
                              OrderMapper om, UserFavoriteMapper ufm,
                              PasswordEncoder pe, JwtUtil jwt, UserContextHolder uch,
                              LoginRateLimiter loginRateLimiter) {
        this.userMapper = um;
        this.userRoleMapper = urm;
        this.roleMapper = rm;
        this.customerMapper = cm;
        this.orderMapper = om;
        this.userFavoriteMapper = ufm;
        this.passwordEncoder = pe;
        this.jwtUtil = jwt;
        this.userContextHolder = uch;
        this.loginRateLimiter = loginRateLimiter;
    }

    @ApiOperation("C端用户注册")
    @PostMapping("/register")
    public ApiResult<LoginResultDTO> register(@RequestBody Map<String, String> body, HttpServletResponse response) {
        String username = body.get("username");
        String password = body.get("password");
        String phone = body.get("phone");

        if (username == null || username.trim().isEmpty() || password == null || password.trim().isEmpty()) {
            return ApiResult.fail("用户名和密码不能为空");
        }
        long exist = userMapper.selectCount(new LambdaQueryWrapper<User>()
                .eq(User::getUsername, username.trim()));
        if (exist > 0) {
            return ApiResult.fail("用户名已存在");
        }

        User user = new User();
        user.setUsername(username.trim());
        user.setPasswordHash(passwordEncoder.encode(password));
        user.setRealName(username.trim());
        user.setPhone(phone);
        user.setStatus("ENABLED");
        userMapper.insert(user);

        // 绑定 CUSTOMER 角色
        Role customerRole = roleMapper.selectOne(new LambdaQueryWrapper<Role>()
                .eq(Role::getRoleCode, "ROLE_CUSTOMER"));
        if (customerRole == null) {
            customerRole = roleMapper.selectOne(new LambdaQueryWrapper<Role>()
                    .eq(Role::getRoleCode, "ROLE_ASSOCIATE"));
        }
        if (customerRole != null) {
            UserRole ur = new UserRole();
            ur.setUserId(user.getId());
            ur.setRoleId(customerRole.getId());
            userRoleMapper.insert(ur);
        }

        // 同步创建客户记录到 customer 表，确保管理后台可见
        Customer customer = new Customer();
        customer.setUserId(user.getId());
        customer.setName(user.getRealName());
        customer.setPhone(user.getPhone());
        customer.setCode("KH" + System.currentTimeMillis() % 100000000);
        customer.setLevel("normal");
        customer.setStatus("normal");
        customer.setRegisteredAt(LocalDate.now().toString());
        customer.setTotalConsumption(BigDecimal.ZERO);
        customer.setPoints(0);
        customerMapper.insert(customer);

        String roleCode = customerRole != null ? customerRole.getRoleCode() : "ROLE_CUSTOMER";

        Map<String, Object> claimsMap = new HashMap<>();
        claimsMap.put("userId", user.getId());
        claimsMap.put("username", user.getUsername());
        claimsMap.put("roles", roleCode);
        claimsMap.put("tokenVersion", user.getTokenVersion() != null ? user.getTokenVersion() : 0);
        String token = jwtUtil.generateToken(claimsMap);
        setTokenCookie(response, token);

        return ApiResult.ok(new LoginResultDTO()
                .token(token)
                .userId(user.getId())
                .username(user.getUsername())
                .realName(user.getRealName())
                .avatar(user.getAvatar())
                .phone(user.getPhone())
                .roles(Collections.singletonList(roleCode)));
    }

    @ApiOperation("C端用户登录（仅限CUSTOMER角色）")
    @PostMapping("/login")
    public ApiResult<LoginResultDTO> login(@RequestBody Map<String, String> body, HttpServletResponse response) {
        String username = body.get("username");
        String password = body.get("password");

        if (username == null || password == null) {
            return ApiResult.fail("用户名和密码不能为空");
        }

        String key = "shop:" + username.trim();
        if (loginRateLimiter.isLocked(key)) {
            return ApiResult.fail(429, "登录失败次数过多，请15分钟后再试");
        }

        User user = userMapper.selectOne(new LambdaQueryWrapper<User>()
                .eq(User::getUsername, username.trim()));
        if (user == null) {
            loginRateLimiter.onFailure(key);
            return ApiResult.fail("用户名或密码错误");
        }
        if (!passwordEncoder.matches(password, user.getPasswordHash())) {
            loginRateLimiter.onFailure(key);
            return ApiResult.fail("用户名或密码错误");
        }
        if ("DISABLED".equals(user.getStatus())) {
            return ApiResult.fail("账号已被禁用");
        }

        // 校验用户必须有 CUSTOMER 角色
        List<Long> roleIds = userRoleMapper.selectList(
                new LambdaQueryWrapper<UserRole>().eq(UserRole::getUserId, user.getId()))
                .stream().map(UserRole::getRoleId).collect(Collectors.toList());
        List<Role> roles = roleIds.isEmpty() ? Collections.emptyList()
                : roleMapper.selectBatchIds(roleIds);
        List<String> roleCodes = roles.stream().map(Role::getRoleCode).collect(Collectors.toList());

        boolean isCustomer = roleCodes.stream().anyMatch(r ->
                "ROLE_CUSTOMER".equals(r) || "ROLE_ASSOCIATE".equals(r));
        if (!isCustomer) {
            return ApiResult.fail("该账号非商城用户，请使用管理后台登录");
        }

        // 更新最后登录时间
        user.setLastLoginAt(LocalDateTime.now());
        userMapper.updateById(user);

        Map<String, Object> claimsMap = new HashMap<>();
        claimsMap.put("userId", user.getId());
        claimsMap.put("username", user.getUsername());
        claimsMap.put("storeId", user.getStoreId());
        claimsMap.put("regionId", user.getRegionId());
        claimsMap.put("roles", String.join(",", roleCodes));
        claimsMap.put("tokenVersion", user.getTokenVersion() != null ? user.getTokenVersion() : 0);
        String token = jwtUtil.generateToken(claimsMap);
        loginRateLimiter.onSuccess(key);
        setTokenCookie(response, token);

        return ApiResult.ok(new LoginResultDTO()
                .token(token)
                .userId(user.getId())
                .username(user.getUsername())
                .realName(user.getRealName())
                .avatar(user.getAvatar())
                .phone(user.getPhone())
                .email(user.getEmail())
                .storeId(user.getStoreId())
                .regionId(user.getRegionId())
                .roles(roleCodes));
    }

    @PreAuthorize("isAuthenticated()")
    @ApiOperation("获取当前C端用户信息")
    @GetMapping("/me")
    public ApiResult<LoginResultDTO> me() {
        Long userId = userContextHolder.getUserId();
        if (userId == null) {
            return ApiResult.fail(401, "未登录");
        }
        User user = userMapper.selectById(userId);
        if (user == null) {
            return ApiResult.fail("用户不存在");
        }

        List<Long> roleIds = userRoleMapper.selectList(
                new LambdaQueryWrapper<UserRole>().eq(UserRole::getUserId, userId))
                .stream().map(UserRole::getRoleId).collect(Collectors.toList());
        List<String> roleCodes = roleIds.isEmpty() ? Collections.emptyList()
                : roleMapper.selectBatchIds(roleIds).stream()
                .map(Role::getRoleCode).collect(Collectors.toList());

        return ApiResult.ok(new LoginResultDTO()
                .userId(user.getId())
                .username(user.getUsername())
                .realName(user.getRealName())
                .avatar(user.getAvatar())
                .phone(user.getPhone())
                .email(user.getEmail())
                .timezone(user.getTimezone())
                .language(user.getLanguage())
                .dateFormat(user.getDateFormat())
                .notifySystem(user.getNotifySystem())
                .notifyOrder(user.getNotifyOrder())
                .notifyInventory(user.getNotifyInventory())
                .notifyMarketing(user.getNotifyMarketing())
                .storeId(user.getStoreId())
                .regionId(user.getRegionId())
                .position(user.getPosition())
                .entryDate(user.getEntryDate())
                .createdAt(user.getCreatedAt())
                .lastLoginAt(user.getLastLoginAt())
                .roles(roleCodes));
    }

    @PreAuthorize("isAuthenticated()")
    @ApiOperation("更新C端个人信息")
    @PutMapping("/profile")
    public ApiResult<Void> updateProfile(@RequestBody Map<String, Object> body) {
        Long userId = userContextHolder.getUserId();
        User user = userMapper.selectById(userId);
        if (user == null) return ApiResult.fail("用户不存在");
        if (body.containsKey("phone")) user.setPhone(asString(body.get("phone")));
        if (body.containsKey("email")) user.setEmail(asString(body.get("email")));
        if (body.containsKey("avatar")) user.setAvatar(asString(body.get("avatar")));
        if (body.containsKey("realName")) user.setRealName(asString(body.get("realName")));
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

    @PreAuthorize("isAuthenticated()")
    @ApiOperation("C端用户登出")
    @PostMapping("/logout")
    public ApiResult<Void> logout(HttpServletResponse response) {
        clearTokenCookie(response);
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

    @PreAuthorize("isAuthenticated()")
    @ApiOperation("C端用户注销账号（软禁用）")
    @DeleteMapping("/account")
    public ApiResult<Void> deactivate() {
        Long userId = userContextHolder.getUserId();
        if (userId == null) return ApiResult.fail(401, "未登录");
        User user = userMapper.selectById(userId);
        if (user == null) return ApiResult.fail("用户不存在");
        user.setStatus("DISABLED");
        userMapper.updateById(user);
        return ApiResult.ok();
    }

    @PreAuthorize("isAuthenticated()")
    @ApiOperation("获取C端用户统计数据")
    @GetMapping("/stats")
    public ApiResult<Map<String, Object>> stats() {
        Long userId = userContextHolder.getUserId();
        if (userId == null) return ApiResult.fail(401, "未登录");

        Map<String, Object> stats = new LinkedHashMap<>();
        stats.put("orderCount", orderMapper.selectCount(
                new LambdaQueryWrapper<Order>().eq(Order::getUserId, userId)));
        stats.put("favoriteCount", userFavoriteMapper.selectCount(
                new LambdaQueryWrapper<UserFavorite>().eq(UserFavorite::getUserId, userId)));
        stats.put("couponCount", 0);
        return ApiResult.ok(stats);
    }

    private String asString(Object v) {
        return v == null ? null : String.valueOf(v);
    }

    private Boolean asBool(Object v) {
        if (v == null) return null;
        if (v instanceof Boolean) return (Boolean) v;
        return "true".equalsIgnoreCase(String.valueOf(v)) || "1".equals(String.valueOf(v));
    }

    // ---- Cookie 工具 ----

    /** 设置商城端 Cookie（不主动清除管理端，允许双端共存；AuthInterceptor 按路径分区选取） */
    private void setTokenCookie(HttpServletResponse response, String token) {
        String secure = cookieSecure ? "; Secure" : "";
        response.addHeader("Set-Cookie", String.format(
            "zbt_shop_token=%s; HttpOnly%s; Path=/; Max-Age=259200; SameSite=Lax", token, secure));
    }

    private void clearTokenCookie(HttpServletResponse response) {
        String secure = cookieSecure ? "; Secure" : "";
        response.addHeader("Set-Cookie",
            "zbt_shop_token=; HttpOnly" + secure + "; Path=/; Max-Age=0; SameSite=Lax");
    }
}
