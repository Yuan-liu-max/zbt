package com.zhubao.manage.module.product.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhubao.manage.common.dto.PageDTO;
import com.zhubao.manage.module.product.entity.ProductBrand;
import com.zhubao.manage.module.product.mapper.ProductBrandMapper;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ProductBrandService {

    private final ProductBrandMapper mapper;
    public ProductBrandService(ProductBrandMapper m) { this.mapper = m; }

    public IPage<ProductBrand> page(PageDTO dto, String name, String status) {
        LambdaQueryWrapper<ProductBrand> w = new LambdaQueryWrapper<>();
        if (StringUtils.isNotBlank(name)) w.like(ProductBrand::getName, name);
        if (StringUtils.isNotBlank(status)) w.eq(ProductBrand::getStatus, status);
        w.orderByAsc(ProductBrand::getSortOrder);
        w.orderByDesc(ProductBrand::getCreatedAt);
        return mapper.selectPage(new Page<>(dto.getPageNum(), dto.getPageSize()), w);
    }

    public List<ProductBrand> listAll() {
        LambdaQueryWrapper<ProductBrand> w = new LambdaQueryWrapper<>();
        w.orderByAsc(ProductBrand::getSortOrder);
        w.orderByDesc(ProductBrand::getCreatedAt);
        return mapper.selectList(w);
    }

    public ProductBrand detail(Long id) { return mapper.selectById(id); }

    @Transactional
    public ProductBrand create(ProductBrand b) { mapper.insert(b); return b; }

    @Transactional
    public ProductBrand update(Long id, ProductBrand b) { b.setId(id); mapper.updateById(b); return detail(id); }

    @Transactional
    public void delete(Long id) { mapper.deleteById(id); }
}
