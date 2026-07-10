package com.zhubao.manage.module.product.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhubao.manage.common.dto.PageDTO;
import com.zhubao.manage.common.exception.BusinessException;
import com.zhubao.manage.common.exception.ErrorCode;
import com.zhubao.manage.module.product.entity.*;
import com.zhubao.manage.module.product.mapper.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class ProductService {

    private final ProductMapper productMapper;
    private final ProductInventoryCheckMapper inventoryCheckMapper;
    private final ProductMaintenanceCheckMapper maintenanceCheckMapper;
    private final ProductSalesAnalysisMapper salesAnalysisMapper;
    private final NewProductPlanMapper newProductPlanMapper;
    private final PromotionPlanMapper promotionPlanMapper;

    public ProductService(ProductMapper pm, ProductInventoryCheckMapper ic,
                          ProductMaintenanceCheckMapper mc, ProductSalesAnalysisMapper sa,
                          NewProductPlanMapper np, PromotionPlanMapper pp) {
        this.productMapper = pm; this.inventoryCheckMapper = ic;
        this.maintenanceCheckMapper = mc; this.salesAnalysisMapper = sa;
        this.newProductPlanMapper = np; this.promotionPlanMapper = pp;
    }

    // ---- 通用 ----
    public <T> List<T> all(BaseMapper<T> m, LambdaQueryWrapper<T> w) { return m.selectList(w); }
    public <T> IPage<T> page(BaseMapper<T> m, PageDTO page, LambdaQueryWrapper<T> w) {
        return m.selectPage(new Page<>(page.getPageNum(), page.getPageSize()), w);
    }
    public <T> T get(BaseMapper<T> m, Long id, String name) {
        T t = m.selectById(id); if (t == null) throw new BusinessException(ErrorCode.DATA_NOT_FOUND.getCode(), name + "不存在"); return t;
    }
    public <T> void save(BaseMapper<T> m, T t) { m.insert(t); }
    public <T> void update(BaseMapper<T> m, T t) { m.updateById(t); }
    public <T> void del(BaseMapper<T> m, Long id) { m.deleteById(id); }

    // ---- 商品敏感字段过滤 ----

    /**
     * 根据角色过滤敏感字段 costPrice/grossMarginRate
     * 仅店长/区域经理/总部/管理员可见
     */
    public List<Product> listProductsWithRoleFilter(List<String> roles) {
        List<Product> list = productMapper.selectList(new LambdaQueryWrapper<Product>()
                .orderByDesc(Product::getCreatedAt));
        if (!canViewSensitivePrice(roles)) {
            for (Product p : list) { p.setCostPrice(null); p.setGrossMarginRate(null); }
        }
        return list;
    }

    public Product getProductWithRoleFilter(Long id, List<String> roles) {
        Product p = get(productMapper, id, "商品");
        if (!canViewSensitivePrice(roles)) { p.setCostPrice(null); p.setGrossMarginRate(null); }
        return p;
    }

    private boolean canViewSensitivePrice(List<String> roles) {
        if (roles == null || roles.isEmpty()) return false;
        return roles.contains("ROLE_ADMIN") || roles.contains("ROLE_HQ")
                || roles.contains("ROLE_REGIONAL") || roles.contains("ROLE_MANAGER");
    }
}
