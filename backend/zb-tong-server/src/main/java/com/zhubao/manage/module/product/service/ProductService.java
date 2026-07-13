package com.zhubao.manage.module.product.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhubao.manage.common.dto.PageDTO;
import com.zhubao.manage.common.exception.BusinessException;
import com.zhubao.manage.common.exception.ErrorCode;
import com.zhubao.manage.common.interceptor.UserContextHolder;
import com.zhubao.manage.module.product.entity.*;
import com.zhubao.manage.module.product.mapper.*;
import com.zhubao.manage.module.role.entity.Role;
import com.zhubao.manage.module.role.entity.UserRole;
import com.zhubao.manage.module.role.mapper.RoleMapper;
import com.zhubao.manage.module.role.mapper.UserRoleMapper;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProductService {

    private final ProductMapper productMapper;
    private final ProductInventoryCheckMapper inventoryCheckMapper;
    private final ProductMaintenanceCheckMapper maintenanceCheckMapper;
    private final ProductSalesAnalysisMapper salesAnalysisMapper;
    private final NewProductPlanMapper newProductPlanMapper;
    private final PromotionPlanMapper promotionPlanMapper;
    private final UserContextHolder userContextHolder;
    private final UserRoleMapper userRoleMapper;
    private final RoleMapper roleMapper;

    public ProductService(ProductMapper pm, ProductInventoryCheckMapper ic,
                          ProductMaintenanceCheckMapper mc, ProductSalesAnalysisMapper sa,
                          NewProductPlanMapper np, PromotionPlanMapper pp,
                          UserContextHolder uch, UserRoleMapper urm, RoleMapper rm) {
        this.productMapper = pm; this.inventoryCheckMapper = ic;
        this.maintenanceCheckMapper = mc; this.salesAnalysisMapper = sa;
        this.newProductPlanMapper = np; this.promotionPlanMapper = pp;
        this.userContextHolder = uch; this.userRoleMapper = urm; this.roleMapper = rm;
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

    /** 获取当前用户角色码列表 */
    private List<String> resolveUserRoles() {
        Long userId = userContextHolder.getUserId();
        if (userId == null) return Collections.emptyList();
        try {
            List<Long> roleIds = userRoleMapper.selectList(
                    new LambdaQueryWrapper<UserRole>().eq(UserRole::getUserId, userId))
                    .stream().map(UserRole::getRoleId).collect(Collectors.toList());
            if (roleIds.isEmpty()) return Collections.emptyList();
            return roleMapper.selectBatchIds(roleIds).stream()
                    .map(Role::getRoleCode).collect(Collectors.toList());
        } catch (Exception e) { return Collections.emptyList(); }
    }

    /** 列表（自动角色过滤） */
    public List<Product> listFiltered() {
        return listProductsWithRoleFilter(resolveUserRoles());
    }

    /** 详情（自动角色过滤） */
    public Product getFiltered(Long id) {
        return getProductWithRoleFilter(id, resolveUserRoles());
    }

    /** 列表（显式传角色） */
    public List<Product> listProductsWithRoleFilter(List<String> roles) {
        List<Product> list = productMapper.selectList(new LambdaQueryWrapper<Product>()
                .orderByDesc(Product::getCreatedAt));
        if (!canViewSensitivePrice(roles)) {
            for (Product p : list) { p.setCostPrice(null); p.setGrossMarginRate(null); }
        }
        return list;
    }

    /** 详情（显式传角色） */
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
