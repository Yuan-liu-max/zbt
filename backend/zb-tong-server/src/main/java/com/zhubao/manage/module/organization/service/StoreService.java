package com.zhubao.manage.module.organization.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhubao.manage.common.dto.PageDTO;
import com.zhubao.manage.common.exception.BusinessException;
import com.zhubao.manage.common.exception.ErrorCode;
import com.zhubao.manage.module.organization.entity.Store;
import com.zhubao.manage.module.organization.mapper.StoreMapper;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class StoreService {

    private final StoreMapper storeMapper;

    public StoreService(StoreMapper storeMapper) { this.storeMapper = storeMapper; }

    public IPage<Store> page(PageDTO dto, String keyword) {
        Page<Store> page = new Page<>(dto.getPageNum(), dto.getPageSize());
        LambdaQueryWrapper<Store> w = new LambdaQueryWrapper<>();
        if (StringUtils.isNotBlank(keyword)) {
            w.and(q -> q.like(Store::getStoreName, keyword).or().like(Store::getStoreCode, keyword));
        }
        w.orderByAsc(Store::getId);
        return storeMapper.selectPage(page, w);
    }

    public List<Store> listAll() {
        return storeMapper.selectList(new LambdaQueryWrapper<Store>().orderByAsc(Store::getId));
    }

    public Store detail(Long id) {
        Store s = storeMapper.selectById(id);
        if (s == null) throw new BusinessException(ErrorCode.STORE_NOT_FOUND);
        return s;
    }

    @Transactional
    public Store create(Store store) {
        storeMapper.insert(store);
        return store;
    }

    @Transactional
    public Store update(Long id, Store store) {
        detail(id);
        store.setId(id);
        storeMapper.updateById(store);
        return detail(id);
    }

    @Transactional
    public void delete(Long id) {
        detail(id);
        storeMapper.deleteById(id);
    }
}
