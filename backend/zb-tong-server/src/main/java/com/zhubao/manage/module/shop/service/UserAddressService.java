package com.zhubao.manage.module.shop.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zhubao.manage.module.shop.entity.UserAddress;
import com.zhubao.manage.module.shop.mapper.UserAddressMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UserAddressService {

    private final UserAddressMapper addressMapper;

    public UserAddressService(UserAddressMapper addressMapper) {
        this.addressMapper = addressMapper;
    }

    /** 获取用户所有地址 */
    public List<UserAddress> listByUser(Long userId) {
        return addressMapper.selectList(
                new LambdaQueryWrapper<UserAddress>()
                        .eq(UserAddress::getUserId, userId)
                        .orderByDesc(UserAddress::getIsDefault)
                        .orderByDesc(UserAddress::getUpdatedAt));
    }

    /** 新增地址 */
    @Transactional
    public UserAddress create(UserAddress addr) {
        // 如果设为默认，先取消其他默认
        if (addr.getIsDefault() != null && addr.getIsDefault() == 1) {
            clearDefault(addr.getUserId());
        }
        addressMapper.insert(addr);
        return addr;
    }

    /** 更新地址 */
    @Transactional
    public void update(Long id, Long userId, UserAddress addr) {
        UserAddress exist = addressMapper.selectById(id);
        if (exist == null || !exist.getUserId().equals(userId)) return;
        if (addr.getIsDefault() != null && addr.getIsDefault() == 1) {
            clearDefault(userId);
        }
        addr.setId(id);
        addr.setUserId(userId);
        addressMapper.updateById(addr);
    }

    /** 删除地址 */
    public void delete(Long id, Long userId) {
        addressMapper.delete(new LambdaQueryWrapper<UserAddress>()
                .eq(UserAddress::getId, id)
                .eq(UserAddress::getUserId, userId));
    }

    /** 设为默认地址 */
    @Transactional
    public void setDefault(Long id, Long userId) {
        clearDefault(userId);
        UserAddress addr = addressMapper.selectById(id);
        if (addr != null && addr.getUserId().equals(userId)) {
            addr.setIsDefault(1);
            addressMapper.updateById(addr);
        }
    }

    private void clearDefault(Long userId) {
        List<UserAddress> defaults = addressMapper.selectList(
                new LambdaQueryWrapper<UserAddress>()
                        .eq(UserAddress::getUserId, userId)
                        .eq(UserAddress::getIsDefault, 1));
        for (UserAddress a : defaults) {
            a.setIsDefault(0);
            addressMapper.updateById(a);
        }
    }
}
