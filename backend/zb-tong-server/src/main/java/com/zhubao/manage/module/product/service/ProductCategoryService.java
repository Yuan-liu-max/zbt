package com.zhubao.manage.module.product.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhubao.manage.common.dto.PageDTO;
import com.zhubao.manage.module.product.entity.ProductCategory;
import com.zhubao.manage.module.product.mapper.ProductCategoryMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ProductCategoryService {

    private final ProductCategoryMapper mapper;
    public ProductCategoryService(ProductCategoryMapper m) { this.mapper = m; }

    public IPage<ProductCategory> page(PageDTO dto) {
        LambdaQueryWrapper<ProductCategory> w = new LambdaQueryWrapper<>();
        w.orderByAsc(ProductCategory::getSortOrder);
        w.orderByDesc(ProductCategory::getCreatedAt);
        return mapper.selectPage(new Page<>(dto.getPageNum(), dto.getPageSize()), w);
    }

    public List<ProductCategory> listAll() {
        LambdaQueryWrapper<ProductCategory> w = new LambdaQueryWrapper<>();
        w.orderByAsc(ProductCategory::getSortOrder);
        w.orderByDesc(ProductCategory::getCreatedAt);
        return mapper.selectList(w);
    }

    public ProductCategory detail(Long id) { return mapper.selectById(id); }

    @Transactional
    public ProductCategory create(ProductCategory c) { mapper.insert(c); return c; }

    @Transactional
    public ProductCategory update(Long id, ProductCategory c) { c.setId(id); mapper.updateById(c); return detail(id); }

    @Transactional
    public void delete(Long id) { mapper.deleteById(id); }

    /** 删除某分类下的所有子分类（级联删除） */
    @Transactional
    public void deleteChildren(Long parentId) {
        mapper.delete(new LambdaQueryWrapper<ProductCategory>()
                .eq(ProductCategory::getParentId, parentId));
    }
}
