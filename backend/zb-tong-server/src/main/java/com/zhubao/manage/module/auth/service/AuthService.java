package com.zhubao.manage.module.auth.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zhubao.manage.common.exception.BusinessException;
import com.zhubao.manage.common.exception.ErrorCode;
import com.zhubao.manage.common.interceptor.UserContextHolder;
import com.zhubao.manage.common.utils.JwtUtil;
import com.zhubao.manage.module.auth.dto.LoginDTO;
import com.zhubao.manage.module.auth.dto.LoginResultDTO;
import com.zhubao.manage.module.customer.mapper.CustomerMapper;
import com.zhubao.manage.module.customer.entity.Customer;
import com.zhubao.manage.module.customer.mapper.CustomerMapper;
import com.zhubao.manage.module.order.mapper.OrderMapper;
import com.zhubao.manage.module.order.entity.Order;
import com.zhubao.manage.module.shop.mapper.UserFavoriteMapper;
import com.zhubao.manage.module.shop.entity.UserFavorite;
import com.zhubao.manage.module.organization.entity.Organization;
import com.zhubao.manage.module.organization.entity.Store;
import com.zhubao.manage.module.organization.mapper.OrganizationMapper;
import com.zhubao.manage.module.organization.mapper.StoreMapper;
import com.zhubao.manage.module.product.mapper.ProductMapper;
import com.zhubao.manage.module.role.entity.*;
import com.zhubao.manage.module.role.mapper.*;
import com.zhubao.manage.module.sales.entity.SalesRecord;
import com.zhubao.manage.module.sales.mapper.SalesRecordMapper;
import com.zhubao.manage.module.user.entity.User;
import com.zhubao.manage.module.user.mapper.UserMapper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class AuthService {

    private final UserMapper userMapper;
    private final UserRoleMapper userRoleMapper;
    private final RoleMapper roleMapper;
    private final RolePermissionMapper rolePermissionMapper;
    private final PermissionMapper permissionMapper;
    private final StoreMapper storeMapper;
    private final OrganizationMapper organizationMapper;
    private final ProductMapper productMapper;
    private final OrderMapper orderMapper;
    private final CustomerMapper customerMapper;
    private final SalesRecordMapper salesRecordMapper;
    private final UserFavoriteMapper userFavoriteMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final UserContextHolder userContextHolder;

    public AuthService(UserMapper userMapper, UserRoleMapper userRoleMapper,
                       RoleMapper roleMapper, RolePermissionMapper rolePermissionMapper,
                       PermissionMapper permissionMapper, StoreMapper storeMapper,
                       OrganizationMapper organizationMapper, ProductMapper productMapper,
                       OrderMapper orderMapper, CustomerMapper customerMapper,
                       SalesRecordMapper salesRecordMapper, UserFavoriteMapper userFavoriteMapper,
                       PasswordEncoder passwordEncoder,
                       JwtUtil jwtUtil, UserContextHolder userContextHolder) {
        this.userMapper = userMapper;
        this.userRoleMapper = userRoleMapper;
        this.roleMapper = roleMapper;
        this.rolePermissionMapper = rolePermissionMapper;
        this.permissionMapper = permissionMapper;
        this.storeMapper = storeMapper;
        this.organizationMapper = organizationMapper;
        this.productMapper = productMapper;
        this.orderMapper = orderMapper;
        this.customerMapper = customerMapper;
        this.salesRecordMapper = salesRecordMapper;
        this.userFavoriteMapper = userFavoriteMapper;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.userContextHolder = userContextHolder;
    }

    /**
     * 登录 —— 验证密码 → 返回 JWT + 用户信息 + 角色 + 权限
     */
    @Transactional
    public LoginResultDTO login(LoginDTO dto) {
        // 查询用户
        User user = userMapper.selectOne(new LambdaQueryWrapper<User>()
                .eq(User::getUsername, dto.getUsername()));
        if (user == null) {
            throw new BusinessException(ErrorCode.LOGIN_FAILED);
        }

        // 验证密码
        if (!passwordEncoder.matches(dto.getPassword(), user.getPasswordHash())) {
            throw new BusinessException(ErrorCode.LOGIN_FAILED);
        }

        // 检查状态
        if ("DISABLED".equals(user.getStatus())) {
            throw new BusinessException(ErrorCode.ACCOUNT_DISABLED);
        }

        // 更新最后登录时间
        user.setLastLoginAt(LocalDateTime.now());
        userMapper.updateById(user);

        // 查询角色和权限
        List<Long> roleIds = userRoleMapper.selectList(
                new LambdaQueryWrapper<UserRole>().eq(UserRole::getUserId, user.getId()))
                .stream().map(UserRole::getRoleId).collect(Collectors.toList());

        List<Role> roles = roleIds.isEmpty() ? Collections.emptyList()
                : roleMapper.selectBatchIds(roleIds);
        List<String> roleCodes = roles.stream().map(Role::getRoleCode).collect(Collectors.toList());

        // 生成 JWT —— 包含 roles 以便拦截器跳过 DB 查询 (P1-5)
        Map<String, Object> claimsMap = new HashMap<>();
        claimsMap.put("userId", user.getId());
        claimsMap.put("username", user.getUsername());
        claimsMap.put("storeId", user.getStoreId());
        claimsMap.put("regionId", user.getRegionId());
        claimsMap.put("roles", String.join(",", roleCodes));
        claimsMap.put("tokenVersion", user.getTokenVersion() != null ? user.getTokenVersion() : 0);
        String token = jwtUtil.generateToken(claimsMap);

        List<Long> permIds = rolePermissionMapper.selectList(
                new LambdaQueryWrapper<RolePermission>().in(RolePermission::getRoleId, roleIds))
                .stream().map(RolePermission::getPermissionId).distinct().collect(Collectors.toList());

        List<String> permCodes = permIds.isEmpty() ? Collections.emptyList()
                : permissionMapper.selectBatchIds(permIds).stream()
                .map(Permission::getPermCode).collect(Collectors.toList());

        return new LoginResultDTO()
                .token(token)
                .userId(user.getId())
                .username(user.getUsername())
                .realName(user.getRealName())
                .avatar(user.getAvatar())
                .storeId(user.getStoreId())
                .regionId(user.getRegionId())
                .roles(roleCodes)
                .permissions(permCodes);
    }

    /**
     * 获取当前登录用户信息
     */
    @Transactional(readOnly = true)
    public LoginResultDTO getCurrentUser() {
        Long userId = userContextHolder.getUserId();
        if (userId == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }

        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }

        List<Long> roleIds = userRoleMapper.selectList(
                new LambdaQueryWrapper<UserRole>().eq(UserRole::getUserId, userId))
                .stream().map(UserRole::getRoleId).collect(Collectors.toList());

        List<String> roleCodes = roleIds.isEmpty() ? Collections.emptyList()
                : roleMapper.selectBatchIds(roleIds).stream()
                .map(Role::getRoleCode).collect(Collectors.toList());

        List<String> roleNames = roleIds.isEmpty() ? Collections.emptyList()
                : roleMapper.selectBatchIds(roleIds).stream()
                .map(Role::getRoleName).collect(Collectors.toList());

        String storeName = null;
        if (user.getStoreId() != null) {
            Store store = storeMapper.selectById(user.getStoreId());
            if (store != null) storeName = store.getStoreName();
        }
        String regionName = null;
        if (user.getRegionId() != null) {
            Organization org = organizationMapper.selectById(user.getRegionId());
            if (org != null) regionName = org.getOrgName();
        }

        return new LoginResultDTO()
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
                .storeName(storeName)
                .regionId(user.getRegionId())
                .regionName(regionName)
                .position(user.getPosition())
                .entryDate(user.getEntryDate())
                .createdAt(user.getCreatedAt())
                .lastLoginAt(user.getLastLoginAt())
                .roles(roleCodes)
                .roleNames(roleNames);
    }

    /**
     * 个人中心数据统计 —— 用户维度数据
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getUserStats() {
        Long userId = userContextHolder.getUserId();
        if (userId == null) throw new BusinessException(ErrorCode.UNAUTHORIZED);

        Map<String, Object> stats = new LinkedHashMap<>();
        // 用户个人的订单数
        stats.put("orderCount", orderMapper.selectCount(
                new LambdaQueryWrapper<Order>().eq(Order::getUserId, userId)));
        // 用户个人的收藏数
        stats.put("favoriteCount", userFavoriteMapper.selectCount(
                new LambdaQueryWrapper<UserFavorite>().eq(UserFavorite::getUserId, userId)));
        // 优惠券数（暂未实现优惠券系统）
        stats.put("couponCount", 0);

        // 以下为系统级参考数据（管理后台使用）
        stats.put("productCount", productMapper.selectCount(null));
        stats.put("customerCount", customerMapper.selectCount(null));

        // 今日成交额
        BigDecimal todaySales = BigDecimal.ZERO;
        List<SalesRecord> todayRecords = salesRecordMapper.selectList(
                new LambdaQueryWrapper<SalesRecord>().eq(SalesRecord::getSalesDate, LocalDate.now()));
        for (SalesRecord r : todayRecords) {
            if (r.getPaidAmount() != null) todaySales = todaySales.add(r.getPaidAmount());
        }
        stats.put("todaySales", todaySales);
        return stats;
    }

    /**
     * 用户注册（默认绑定顾客角色 ROLE_CUSTOMER）
     */
    @Transactional
    public LoginResultDTO register(String username, String password, String phone) {
        if (username == null || username.trim().isEmpty() || password == null || password.trim().isEmpty()) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "用户名和密码不能为空");
        }
        long exist = userMapper.selectCount(new LambdaQueryWrapper<User>()
                .eq(User::getUsername, username.trim()));
        if (exist > 0) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "用户名已存在");
        }

        User user = new User();
        user.setUsername(username.trim());
        user.setPasswordHash(passwordEncoder.encode(password));
        user.setRealName(username.trim());
        user.setPhone(phone);
        user.setStatus("ENABLED");
        userMapper.insert(user);

        // 绑定顾客角色
        Role customerRole = roleMapper.selectOne(new LambdaQueryWrapper<Role>()
                .eq(Role::getRoleCode, "ROLE_CUSTOMER"));
        if (customerRole == null) {
            // 降级：如果 ROLE_CUSTOMER 不存在，使用 ROLE_ASSOCIATE
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

        // 生成 JWT Token
        Map<String, Object> claimsMap = new HashMap<>();
        claimsMap.put("userId", user.getId());
        claimsMap.put("username", user.getUsername());
        claimsMap.put("roles", roleCode);
        claimsMap.put("tokenVersion", user.getTokenVersion() != null ? user.getTokenVersion() : 0);
        String token = jwtUtil.generateToken(claimsMap);

        return new LoginResultDTO()
                .token(token)
                .userId(user.getId())
                .username(user.getUsername())
                .realName(user.getRealName())
                .avatar(user.getAvatar())
                .phone(user.getPhone())
                .roles(Collections.singletonList(roleCode));
    }

    /**
     * 登出（无状态JWT，仅返回成功）
     */
    public void logout() {
        // JWT 无状态，登出仅需前端清除 Token
        // 后续可扩展 Redis 黑名单机制
    }
}
