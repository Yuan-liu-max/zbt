package com.zhubao.manage.module.supplier.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhubao.manage.common.dto.PageDTO;
import com.zhubao.manage.module.supplier.entity.Supplier;
import com.zhubao.manage.module.supplier.mapper.SupplierMapper;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SupplierService {

    private final SupplierMapper supplierMapper;
    public SupplierService(SupplierMapper sm) { this.supplierMapper = sm; }

    public IPage<Supplier> page(PageDTO dto, String name, String contactPerson, String type, String status) {
        LambdaQueryWrapper<Supplier> w = new LambdaQueryWrapper<>();
        if (StringUtils.isNotBlank(name)) w.like(Supplier::getName, name);
        if (StringUtils.isNotBlank(contactPerson)) w.like(Supplier::getContactPerson, contactPerson);
        if (StringUtils.isNotBlank(type)) w.eq(Supplier::getType, type);
        if (StringUtils.isNotBlank(status)) w.eq(Supplier::getStatus, status);
        w.orderByDesc(Supplier::getCreatedAt);
        return supplierMapper.selectPage(new Page<>(dto.getPageNum(), dto.getPageSize()), w);
    }

    public Supplier detail(Long id) { return supplierMapper.selectById(id); }

    @Transactional public Supplier create(Supplier s) { supplierMapper.insert(s); return s; }

    @Transactional public Supplier update(Long id, Supplier s) { s.setId(id); supplierMapper.updateById(s); return detail(id); }

    @Transactional public void delete(Long id) { supplierMapper.deleteById(id); }
}
