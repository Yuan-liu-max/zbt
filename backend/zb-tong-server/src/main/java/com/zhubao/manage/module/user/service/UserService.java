package com.zhubao.manage.module.user.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhubao.manage.common.exception.BusinessException;
import com.zhubao.manage.common.exception.ErrorCode;
import com.zhubao.manage.module.organization.entity.Store;
import com.zhubao.manage.module.organization.mapper.StoreMapper;
import com.zhubao.manage.module.role.entity.Role;
import com.zhubao.manage.module.role.entity.UserRole;
import com.zhubao.manage.module.role.mapper.RoleMapper;
import com.zhubao.manage.module.role.mapper.UserRoleMapper;
import com.zhubao.manage.module.user.dto.UserCreateDTO;
import com.zhubao.manage.module.user.dto.UserQueryDTO;
import com.zhubao.manage.module.user.dto.UserUpdateDTO;
import com.zhubao.manage.module.user.dto.UserVO;
import com.zhubao.manage.module.user.entity.User;
import com.zhubao.manage.module.user.mapper.UserMapper;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class UserService {

    private final UserMapper userMapper;
    private final UserRoleMapper userRoleMapper;
    private final RoleMapper roleMapper;
    private final StoreMapper storeMapper;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserMapper userMapper, UserRoleMapper userRoleMapper,
                       RoleMapper roleMapper, StoreMapper storeMapper,
                       PasswordEncoder passwordEncoder) {
        this.userMapper = userMapper;
        this.userRoleMapper = userRoleMapper;
        this.roleMapper = roleMapper;
        this.storeMapper = storeMapper;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * 分页查询
     */
    @Transactional(readOnly = true)
    public IPage<UserVO> page(UserQueryDTO query) {
        Page<User> page = new Page<>(query.getPageNum(), query.getPageSize());

        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<User>();
        if (StringUtils.isNotBlank(query.getKeyword())) {
            wrapper.and(w -> w.like(User::getUsername, query.getKeyword())
                    .or().like(User::getRealName, query.getKeyword())
                    .or().like(User::getPhone, query.getKeyword()));
        }
        if (query.getStoreId() != null) {
            wrapper.eq(User::getStoreId, query.getStoreId());
        }
        if (StringUtils.isNotBlank(query.getStatus())) {
            wrapper.eq(User::getStatus, query.getStatus());
        }
        wrapper.orderByDesc(User::getCreatedAt);

        IPage<User> userPage = userMapper.selectPage(page, wrapper);

        // 收集 storeId 和 userId 批量查询
        List<Long> storeIds = userPage.getRecords().stream()
                .map(User::getStoreId).filter(id -> id != null).distinct().collect(Collectors.toList());
        List<Long> userIds = userPage.getRecords().stream()
                .map(User::getId).collect(Collectors.toList());

        Map<Long, String> storeNameMap = storeIds.isEmpty() ? Collections.emptyMap()
                : storeMapper.selectBatchIds(storeIds).stream()
                .collect(Collectors.toMap(Store::getId, Store::getStoreName));

        Map<Long, List<UserRole>> userRoleMap = userRoleMapper.selectList(
                new LambdaQueryWrapper<UserRole>().in(UserRole::getUserId, userIds))
                .stream().collect(Collectors.groupingBy(UserRole::getUserId));

        List<Long> allRoleIds = userRoleMap.values().stream()
                .flatMap(List::stream).map(UserRole::getRoleId).distinct().collect(Collectors.toList());
        Map<Long, String> roleNameMap = allRoleIds.isEmpty() ? Collections.emptyMap()
                : roleMapper.selectBatchIds(allRoleIds).stream()
                .collect(Collectors.toMap(Role::getId, Role::getRoleName));

        IPage<UserVO> voPage = new Page<>(query.getPageNum(), query.getPageSize(), userPage.getTotal());
        voPage.setRecords(userPage.getRecords().stream().map(u -> {
            UserVO vo = toVO(u, storeNameMap);
            List<UserRole> urList = userRoleMap.getOrDefault(u.getId(), Collections.emptyList());
            vo.setRoleIds(urList.stream().map(UserRole::getRoleId).collect(Collectors.toList()));
            vo.setRoleNames(urList.stream().map(ur -> roleNameMap.getOrDefault(ur.getRoleId(), ""))
                    .filter(StringUtils::isNotBlank).collect(Collectors.toList()));
            return vo;
        }).collect(Collectors.toList()));

        return voPage;
    }

    /**
     * 用户详情
     */
    @Transactional(readOnly = true)
    public UserVO detail(Long id) {
        User user = getEntityById(id);
        Map<Long, String> storeNameMap;
        if (user.getStoreId() != null) {
            Store store = storeMapper.selectById(user.getStoreId());
            storeNameMap = Collections.singletonMap(user.getStoreId(),
                    store != null ? store.getStoreName() : "");
        } else {
            storeNameMap = Collections.emptyMap();
        }
        UserVO vo = toVO(user, storeNameMap);

        List<UserRole> urList = userRoleMapper.selectList(
                new LambdaQueryWrapper<UserRole>().eq(UserRole::getUserId, id));
        List<Long> roleIds = urList.stream().map(UserRole::getRoleId).collect(Collectors.toList());
        vo.setRoleIds(roleIds);
        if (!roleIds.isEmpty()) {
            vo.setRoleNames(roleMapper.selectBatchIds(roleIds).stream()
                    .map(Role::getRoleName).collect(Collectors.toList()));
        }
        return vo;
    }

    /**
     * 创建用户
     */
    @Transactional
    public UserVO create(UserCreateDTO dto) {
        // 用户名唯一
        Long exist = userMapper.selectCount(
                new LambdaQueryWrapper<User>().eq(User::getUsername, dto.getUsername()));
        if (exist > 0) {
            throw new BusinessException(ErrorCode.USER_ALREADY_EXISTS);
        }

        User user = new User();
        user.setUsername(dto.getUsername());
        user.setPasswordHash(passwordEncoder.encode(dto.getPassword()));
        user.setRealName(dto.getRealName());
        user.setPhone(dto.getPhone());
        user.setStoreId(dto.getStoreId());
        user.setRegionId(dto.getRegionId());
        user.setPosition(dto.getPosition());
        user.setStatus("ACTIVE");
        if (StringUtils.isNotBlank(dto.getEntryDate())) {
            user.setEntryDate(LocalDate.parse(dto.getEntryDate()));
        }
        userMapper.insert(user);

        // 绑定角色
        saveUserRoles(user.getId(), dto.getRoleIds());

        return detail(user.getId());
    }

    /**
     * 更新用户
     */
    @Transactional
    public UserVO update(Long id, UserUpdateDTO dto) {
        User user = getEntityById(id);

        if (StringUtils.isNotBlank(dto.getRealName())) {
            user.setRealName(dto.getRealName());
        }
        if (dto.getPhone() != null) {
            user.setPhone(dto.getPhone());
        }
        if (dto.getStoreId() != null) {
            user.setStoreId(dto.getStoreId());
        }
        if (dto.getRegionId() != null) {
            user.setRegionId(dto.getRegionId());
        }
        if (dto.getPosition() != null) {
            user.setPosition(dto.getPosition());
        }
        if (dto.getStatus() != null) {
            user.setStatus(dto.getStatus());
        }
        if (StringUtils.isNotBlank(dto.getEntryDate())) {
            user.setEntryDate(LocalDate.parse(dto.getEntryDate()));
        }
        userMapper.updateById(user);

        // 更新角色绑定
        if (dto.getRoleIds() != null) {
            saveUserRoles(id, dto.getRoleIds());
        }

        return detail(id);
    }

    /**
     * 删除用户（逻辑删除）
     */
    @Transactional
    public void delete(Long id) {
        getEntityById(id);
        userMapper.deleteById(id);
    }

    private void saveUserRoles(Long userId, List<Long> roleIds) {
        userRoleMapper.delete(new LambdaQueryWrapper<UserRole>().eq(UserRole::getUserId, userId));
        if (roleIds != null && !roleIds.isEmpty()) {
            for (Long roleId : roleIds) {
                UserRole ur = new UserRole();
                ur.setUserId(userId);
                ur.setRoleId(roleId);
                userRoleMapper.insert(ur);
            }
        }
    }

    private User getEntityById(Long id) {
        User user = userMapper.selectById(id);
        if (user == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }
        return user;
    }

    private UserVO toVO(User u, Map<Long, String> storeNameMap) {
        UserVO vo = new UserVO();
        vo.setId(u.getId());
        vo.setUsername(u.getUsername());
        vo.setRealName(u.getRealName());
        vo.setPhone(u.getPhone());
        vo.setAvatar(u.getAvatar());
        vo.setStoreId(u.getStoreId());
        vo.setStoreName(storeNameMap.getOrDefault(u.getStoreId(), null));
        vo.setRegionId(u.getRegionId());
        vo.setPosition(u.getPosition());
        vo.setEntryDate(u.getEntryDate());
        vo.setStatus(u.getStatus());
        vo.setLastLoginAt(u.getLastLoginAt());
        vo.setCreatedAt(u.getCreatedAt());
        return vo;
    }
}
