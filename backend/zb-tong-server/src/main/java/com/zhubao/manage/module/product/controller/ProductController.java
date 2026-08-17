package com.zhubao.manage.module.product.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.zhubao.manage.common.annotation.OperateLog;
import com.zhubao.manage.common.dto.ApiResult;
import com.zhubao.manage.common.dto.PageDTO;
import com.zhubao.manage.common.dto.PageResult;
import com.zhubao.manage.module.product.dto.ProductVO;
import com.zhubao.manage.module.product.entity.*;
import com.zhubao.manage.module.product.mapper.*;
import com.zhubao.manage.module.product.service.ProductService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.math.BigDecimal;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Api(tags = "货品管理")
@RestController
@RequestMapping("/products")
public class ProductController {

    private final ProductService svc;
    private final ProductMapper productMapper;
    private final ProductInventoryCheckMapper inventoryMapper;
    private final ProductMaintenanceCheckMapper maintenanceMapper;
    private final ProductSalesAnalysisMapper salesAnalysisMapper;
    private final NewProductPlanMapper newProductPlanMapper;
    private final PromotionPlanMapper promotionPlanMapper;
    private final com.zhubao.manage.module.organization.mapper.StoreMapper storeMapper;
    private final com.zhubao.manage.module.user.mapper.UserMapper userMapper;
    private final ProductCategoryMapper productCategoryMapper;
    private final com.zhubao.manage.common.interceptor.UserContextHolder userContextHolder;
    private final SecureRandom random = new SecureRandom();

    public ProductController(ProductService svc, ProductMapper pm, ProductInventoryCheckMapper im,
                             ProductMaintenanceCheckMapper mm, ProductSalesAnalysisMapper sm,
                             NewProductPlanMapper nm, PromotionPlanMapper pm2,
                             com.zhubao.manage.module.organization.mapper.StoreMapper stm,
                             com.zhubao.manage.module.user.mapper.UserMapper um,
                             ProductCategoryMapper pcm,
                             com.zhubao.manage.common.interceptor.UserContextHolder uch) {
        this.svc = svc; this.productMapper = pm; this.inventoryMapper = im;
        this.maintenanceMapper = mm; this.salesAnalysisMapper = sm;
        this.newProductPlanMapper = nm; this.promotionPlanMapper = pm2;
        this.storeMapper = stm; this.userMapper = um; this.productCategoryMapper = pcm;
        this.userContextHolder = uch;
    }

    // ===== 商品档案 =====
    @ApiOperation("商品列表（分页+筛选，含角色过滤）") @GetMapping
    public ApiResult<PageResult<ProductVO>> listProducts(@Valid PageDTO dto,
                                                         @RequestParam(required = false) String keyword,
                                                         @RequestParam(required = false) String categoryId,
                                                         @RequestParam(required = false) String status,
                                                         @RequestParam(required = false) Long storeId,
                                                         @RequestParam(required = false) String brandId,
                                                         @RequestParam(required = false) BigDecimal minPrice,
                                                         @RequestParam(required = false) BigDecimal maxPrice,
                                                         @RequestParam(required = false, defaultValue = "createdAt") String sortBy,
                                                         @RequestParam(required = false, defaultValue = "desc") String sortOrder) {
        List<Product> all = svc.listFiltered();
        String statusFilter = StringUtils.isBlank(status) ? null : normalizeStatus(status);
        String categoryFilter = resolveCategoryName(categoryId);
        List<Product> filtered = all.stream()
                .filter(p -> StringUtils.isBlank(keyword)
                        || (p.getProductName() != null && p.getProductName().contains(keyword))
                        || (p.getProductCode() != null && p.getProductCode().contains(keyword)))
                .filter(p -> StringUtils.isBlank(categoryFilter)
                        || (p.getCategory() != null && p.getCategory().contains(categoryFilter))
                        || (p.getStyle() != null && p.getStyle().contains(categoryFilter)))
                .filter(p -> StringUtils.isBlank(brandId)
                        || (p.getStyle() != null && p.getStyle().equals(brandId)))
                .filter(p -> StringUtils.isBlank(statusFilter) || statusFilter.equals(p.getStatus()))
                .filter(p -> storeId == null || storeId.equals(p.getStoreId()))
                .filter(p -> minPrice == null || (p.getRetailPrice() != null && p.getRetailPrice().compareTo(minPrice) >= 0))
                .filter(p -> maxPrice == null || (p.getRetailPrice() != null && p.getRetailPrice().compareTo(maxPrice) <= 0))
                .collect(Collectors.toList());

        // 排序
        Comparator<Product> comparator;
        switch (sortBy) {
            case "price":
                comparator = Comparator.comparing(Product::getRetailPrice, Comparator.nullsLast(Comparator.naturalOrder()));
                break;
            case "name":
                comparator = Comparator.comparing(Product::getProductName, Comparator.nullsLast(Comparator.naturalOrder()));
                break;
            default: // createdAt
                comparator = Comparator.comparing(Product::getCreatedAt, Comparator.nullsLast(Comparator.naturalOrder()));
                break;
        }
        if ("asc".equalsIgnoreCase(sortOrder)) {
            filtered.sort(comparator);
        } else {
            filtered.sort(comparator.reversed());
        }

        int start = (int) ((dto.getPageNum() - 1) * dto.getPageSize());
        List<Product> page = filtered.stream().skip(start).limit(dto.getPageSize()).collect(Collectors.toList());
        fillStockAndStoreName(page);
        List<ProductVO> vos = page.stream().map(ProductVO::from).collect(Collectors.toList());
        vos.forEach(v -> v.setStatus(displayStatus(v.getStatus())));
        return ApiResult.ok(new PageResult<>(dto.getPageNum(), dto.getPageSize(), filtered.size(), vos));
    }

    @ApiOperation("热门搜索关键词") @GetMapping("/search/hot")
    public ApiResult<List<String>> hotSearchKeywords() {
        // 从商品名称中提取高频词作为热门搜索
        List<Product> all = productMapper.selectList(null);
        Map<String, Long> wordCount = new java.util.LinkedHashMap<>();
        for (Product p : all) {
            if (p.getProductName() != null) {
                String name = p.getProductName();
                // 提取2-4字关键词
                for (int len = 4; len >= 2; len--) {
                    for (int i = 0; i + len <= name.length(); i++) {
                        String word = name.substring(i, i + len);
                        wordCount.merge(word, 1L, Long::sum);
                    }
                }
            }
        }
        // 按频率降序取前10个
        List<String> hot = wordCount.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(10)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
        return ApiResult.ok(hot);
    }

    @ApiOperation("首页推荐商品") @GetMapping("/recommend")
    public ApiResult<List<ProductVO>> recommendProducts(
            @RequestParam(required = false, defaultValue = "10") int limit) {
        List<Product> all = svc.listFiltered().stream()
                .filter(p -> "ON_SALE".equals(p.getStatus()))
                .collect(Collectors.toList());
        // 优先按创建时间倒序 + 有库存排前面
        all.sort(Comparator
                .comparing(Product::getStock, Comparator.nullsLast(Comparator.reverseOrder()))
                .thenComparing(Product::getCreatedAt, Comparator.nullsLast(Comparator.reverseOrder())));
        List<Product> top = all.stream().limit(limit).collect(Collectors.toList());
        fillStockAndStoreName(top);
        List<ProductVO> vos = top.stream().map(ProductVO::from).collect(Collectors.toList());
        vos.forEach(v -> v.setStatus(displayStatus(v.getStatus())));
        return ApiResult.ok(vos);
    }
    @ApiOperation("商品分页") @GetMapping("/page")
    public ApiResult<PageResult<Product>> pageProducts(@Valid PageDTO dto) {
        IPage<Product> r = svc.page(productMapper, dto, new LambdaQueryWrapper<Product>().orderByDesc(Product::getCreatedAt));
        return ApiResult.ok(PageResult.of(r)); }
    @ApiOperation("商品详情（含角色过滤）") @GetMapping("/{id}")
    public ApiResult<ProductVO> getProduct(@PathVariable Long id) {
        Product p = svc.getFiltered(id);
        if (p == null) return ApiResult.fail("商品不存在");
        ProductVO vo = ProductVO.from(p);
        vo.setStatus(displayStatus(vo.getStatus()));
        return ApiResult.ok(vo); }
    @PreAuthorize("isAuthenticated()")
    @OperateLog(module = "商品管理", action = "CREATE", targetType = "PRODUCT")
    @ApiOperation("新增商品") @PostMapping
    public ApiResult<Product> createProduct(@Valid @RequestBody Product e) {
        if (StringUtils.isBlank(e.getProductCode())) {
            e.setProductCode("P-" + (System.currentTimeMillis() % 100000000) + (100 + random.nextInt(900)));
        }
        e.setStatus(normalizeStatus(e.getStatus()));
        svc.save(productMapper, e);
        Product saved = productMapper.selectById(e.getId());
        return ApiResult.ok(saved); }
    @PreAuthorize("isAuthenticated()")
    @OperateLog(module = "商品管理", action = "UPDATE", targetType = "PRODUCT", targetIdExpr = "#id")
    @ApiOperation("更新商品") @PutMapping("/{id}")
    public ApiResult<Product> updateProduct(@PathVariable Long id, @Valid @RequestBody Product e) {
        e.setId(id);
        e.setStatus(normalizeStatus(e.getStatus()));
        svc.update(productMapper, e);
        Product updated = productMapper.selectById(id);
        return ApiResult.ok(updated); }
    @PreAuthorize("isAuthenticated()")
    @OperateLog(module = "商品管理", action = "DELETE", targetType = "PRODUCT", targetIdExpr = "#id")
    @ApiOperation("删除商品") @DeleteMapping("/{id}")
    public ApiResult<Void> deleteProduct(@PathVariable Long id) { svc.del(productMapper, id); return ApiResult.ok(); }

    // ---- 状态映射：前端用 on/off，存储用 ON_SALE/OFF_SHELF ----
    private String normalizeStatus(String status) {
        if (StringUtils.isBlank(status)) return "ON_SALE";
        if ("on".equals(status)) return "ON_SALE";
        if ("off".equals(status)) return "OFF_SHELF";
        return status;
    }

    private String displayStatus(String status) {
        return "ON_SALE".equals(status) ? "on" : "off";
    }

    /** categoryId 可能是分类表数字ID（需解析为名称）或 style 字符串 */
    private String resolveCategoryName(String categoryId) {
        if (StringUtils.isBlank(categoryId)) return null;
        if (StringUtils.isNumeric(categoryId)) {
            ProductCategory cat = productCategoryMapper.selectById(Long.valueOf(categoryId));
            return cat != null ? cat.getName() : null;
        }
        return categoryId;
    }

    private String getString(Map<String, Object> body, String key) {
        Object v = body.get(key);
        return v != null && !"".equals(v) ? v.toString() : null;
    }
    private Long getLong(Map<String, Object> body, String key) {
        Object v = body.get(key);
        if (v == null || "".equals(v)) return null;
        return v instanceof Number ? ((Number) v).longValue() : Long.valueOf(v.toString());
    }
    private java.time.LocalDate parseDate(String val) {
        if (StringUtils.isBlank(val)) return null;
        try { return java.time.LocalDate.parse(val); } catch (Exception e) { return null; }
    }

    // ===== 盘点 =====
    @ApiOperation("盘点分页") @GetMapping("/inventory-checks")
    public ApiResult<PageResult<java.util.Map<String, Object>>> pageInventories(@Valid PageDTO dto,
            @RequestParam(required = false) String checkCode,
            @RequestParam(required = false) String warehouse,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        LambdaQueryWrapper<ProductInventoryCheck> w = new LambdaQueryWrapper<>();
        if (StringUtils.isNotBlank(checkCode)) w.like(ProductInventoryCheck::getCheckCode, checkCode);
        if (StringUtils.isNotBlank(status)) w.eq(ProductInventoryCheck::getStatus, status);
        if (StringUtils.isNotBlank(startDate)) w.ge(ProductInventoryCheck::getStartDate, startDate);
        if (StringUtils.isNotBlank(endDate)) w.le(ProductInventoryCheck::getEndDate, endDate);
        w.orderByDesc(ProductInventoryCheck::getCreatedAt);
        IPage<ProductInventoryCheck> r = svc.page(inventoryMapper, dto, w);
        return ApiResult.ok(new PageResult<>(r.getCurrent(), r.getSize(), r.getTotal(),
                toInventoryVO(r.getRecords())));
    }
    @ApiOperation("盘点详情") @GetMapping("/inventory-checks/{id}")
    public ApiResult<Map<String, Object>> getInventory(@PathVariable Long id) {
        ProductInventoryCheck c = svc.get(inventoryMapper, id, "盘点");
        return ApiResult.ok(toInventoryVO(java.util.Collections.singletonList(c)).get(0));
    }
    @OperateLog(module = "商品管理", action = "CREATE", targetType = "INVENTORY")
    @ApiOperation("新增盘点") @PostMapping("/inventory-checks")
    public ApiResult<Void> createInventory(@RequestBody Map<String, Object> body) {
        ProductInventoryCheck e = new ProductInventoryCheck();
        e.setCheckCode(getString(body, "checkCode"));
        if (StringUtils.isBlank(e.getCheckCode())) {
            e.setCheckCode("CK" + (System.currentTimeMillis() % 100000000) + (100 + random.nextInt(900)));
        }
        e.setCheckName(getString(body, "checkName"));
        e.setCheckType(getString(body, "checkType"));
        e.setStartDate(parseDate(getString(body, "startDate")));
        e.setEndDate(parseDate(getString(body, "endDate")));
        e.setStatus(getString(body, "status"));
        if (StringUtils.isBlank(e.getStatus())) e.setStatus("planning");
        e.setStoreId(getLong(body, "storeId"));
        e.setCheckedBy(getLong(body, "checkedBy"));
        if (e.getCheckedBy() == null) e.setCheckedBy(userContextHolder.getUserId());
        e.setWarehouse(getString(body, "warehouse"));
        e.setRemark(getString(body, "remark"));
        if (e.getCheckDate() == null) e.setCheckDate(e.getStartDate() != null ? e.getStartDate() : java.time.LocalDate.now());
        e.setTotalCheckedCount(body.get("totalCheckedCount") != null ? ((Number) body.get("totalCheckedCount")).intValue() : 0);
        e.setAbnormalCount(body.get("abnormalCount") != null ? ((Number) body.get("abnormalCount")).intValue() : 0);
        svc.save(inventoryMapper, e);
        return ApiResult.ok();
    }
    @OperateLog(module = "商品管理", action = "UPDATE", targetType = "INVENTORY", targetIdExpr = "#id")
    @ApiOperation("更新盘点") @PutMapping("/inventory-checks/{id}")
    public ApiResult<Void> updateInventory(@PathVariable Long id, @Valid @RequestBody ProductInventoryCheck e) {
        e.setId(id);
        // 只更新非空字段，避免覆盖已有数据
        ProductInventoryCheck existing = inventoryMapper.selectById(id);
        if (existing == null) return ApiResult.fail("盘点不存在");
        ProductInventoryCheck merged = new ProductInventoryCheck();
        merged.setId(id);
        merged.setCheckCode(e.getCheckCode() != null ? e.getCheckCode() : existing.getCheckCode());
        merged.setCheckName(e.getCheckName() != null ? e.getCheckName() : existing.getCheckName());
        merged.setCheckType(e.getCheckType() != null ? e.getCheckType() : existing.getCheckType());
        merged.setStartDate(e.getStartDate() != null ? e.getStartDate() : existing.getStartDate());
        merged.setEndDate(e.getEndDate() != null ? e.getEndDate() : existing.getEndDate());
        merged.setStatus(e.getStatus() != null ? e.getStatus() : existing.getStatus());
        merged.setCheckDate(e.getCheckDate() != null ? e.getCheckDate() : existing.getCheckDate());
        merged.setStoreId(e.getStoreId() != null ? e.getStoreId() : existing.getStoreId());
        merged.setCheckedBy(e.getCheckedBy() != null ? e.getCheckedBy() : existing.getCheckedBy());
        merged.setTotalCheckedCount(e.getTotalCheckedCount() != null ? e.getTotalCheckedCount() : existing.getTotalCheckedCount());
        merged.setAbnormalCount(e.getAbnormalCount() != null ? e.getAbnormalCount() : existing.getAbnormalCount());
        merged.setAbnormalItems(e.getAbnormalItems() != null ? e.getAbnormalItems() : existing.getAbnormalItems());
        merged.setPhotos(e.getPhotos() != null ? e.getPhotos() : existing.getPhotos());
        merged.setRemark(e.getRemark() != null ? e.getRemark() : existing.getRemark());
        svc.update(inventoryMapper, merged);
        return ApiResult.ok();
    }
    @OperateLog(module = "商品管理", action = "DELETE", targetType = "INVENTORY", targetIdExpr = "#id")
    @ApiOperation("删除盘点") @DeleteMapping("/inventory-checks/{id}")
    public ApiResult<Void> deleteInventory(@PathVariable Long id) { svc.del(inventoryMapper, id); return ApiResult.ok(); }

    /** 盘点记录 → 前端 InventoryCheckRecord 结构（warehouse=门店名, creator=创建人） */
    private List<java.util.Map<String, Object>> toInventoryVO(List<ProductInventoryCheck> list) {
        java.util.Map<Long, String> storeMap = new java.util.HashMap<>();
        java.util.Set<Long> storeIds = new java.util.HashSet<>();
        java.util.Set<Long> userIds = new java.util.HashSet<>();
        for (ProductInventoryCheck c : list) {
            if (c.getStoreId() != null) storeIds.add(c.getStoreId());
            if (c.getCheckedBy() != null) userIds.add(c.getCheckedBy());
        }
        if (!storeIds.isEmpty()) {
            for (com.zhubao.manage.module.organization.entity.Store s : storeMapper.selectBatchIds(storeIds))
                storeMap.put(s.getId(), s.getStoreName());
        }
        java.util.Map<Long, String> userMap = new java.util.HashMap<>();
        if (!userIds.isEmpty()) {
            for (com.zhubao.manage.module.user.entity.User u : userMapper.selectBatchIds(userIds))
                userMap.put(u.getId(), u.getRealName() != null ? u.getRealName() : u.getUsername());
        }
        List<java.util.Map<String, Object>> vos = new ArrayList<>();
        for (ProductInventoryCheck c : list) {
            java.util.Map<String, Object> m = new java.util.LinkedHashMap<>();
            m.put("id", c.getId());
            m.put("checkCode", c.getCheckCode());
            m.put("checkName", c.getCheckName());
            m.put("warehouse", (c.getWarehouse() != null && !c.getWarehouse().isEmpty())
                    ? c.getWarehouse()
                    : (c.getStoreId() != null ? storeMap.getOrDefault(c.getStoreId(), "未知门店") : null));
            m.put("checkType", c.getCheckType());
            m.put("startDate", c.getStartDate() != null ? c.getStartDate().toString() : (c.getCheckDate() != null ? c.getCheckDate().toString() : null));
            m.put("endDate", c.getEndDate() != null ? c.getEndDate().toString() : null);
            m.put("creator", c.getCheckedBy() != null ? userMap.getOrDefault(c.getCheckedBy(), "未知") : null);
            m.put("createdAt", c.getCreatedAt());
            m.put("status", c.getStatus());
            m.put("remark", c.getRemark());
            m.put("totalCheckedCount", c.getTotalCheckedCount());
            m.put("abnormalCount", c.getAbnormalCount());
            vos.add(m);
        }
        return vos;
    }

    // ===== 养护 =====
    @ApiOperation("养护分页") @GetMapping("/maintenance-checks")
    public ApiResult<PageResult<ProductMaintenanceCheck>> pageMaintenances(@Valid PageDTO dto) {
        IPage<ProductMaintenanceCheck> r = svc.page(maintenanceMapper, dto, new LambdaQueryWrapper<>()); return ApiResult.ok(PageResult.of(r)); }
    @ApiOperation("养护详情") @GetMapping("/maintenance-checks/{id}")
    public ApiResult<ProductMaintenanceCheck> getMaintenance(@PathVariable Long id) { return ApiResult.ok(svc.get(maintenanceMapper, id, "养护")); }
    @OperateLog(module = "商品管理", action = "CREATE", targetType = "MAINTENANCE")
    @ApiOperation("新增养护") @PostMapping("/maintenance-checks")
    public ApiResult<Void> createMaintenance(@Valid @RequestBody ProductMaintenanceCheck e) { svc.save(maintenanceMapper, e); return ApiResult.ok(); }
    @OperateLog(module = "商品管理", action = "UPDATE", targetType = "MAINTENANCE", targetIdExpr = "#id")
    @ApiOperation("更新养护") @PutMapping("/maintenance-checks/{id}")
    public ApiResult<Void> updateMaintenance(@PathVariable Long id, @Valid @RequestBody ProductMaintenanceCheck e) { e.setId(id); svc.update(maintenanceMapper, e); return ApiResult.ok(); }
    @OperateLog(module = "商品管理", action = "DELETE", targetType = "MAINTENANCE", targetIdExpr = "#id")
    @ApiOperation("删除养护") @DeleteMapping("/maintenance-checks/{id}")
    public ApiResult<Void> deleteMaintenance(@PathVariable Long id) { svc.del(maintenanceMapper, id); return ApiResult.ok(); }

    // ===== 动销分析 =====
    @ApiOperation("动销分页") @GetMapping("/sales-analyses")
    public ApiResult<PageResult<ProductSalesAnalysis>> pageSalesAnalyses(@Valid PageDTO dto) {
        IPage<ProductSalesAnalysis> r = svc.page(salesAnalysisMapper, dto, new LambdaQueryWrapper<>()); return ApiResult.ok(PageResult.of(r)); }
    @ApiOperation("动销详情") @GetMapping("/sales-analyses/{id}")
    public ApiResult<ProductSalesAnalysis> getSalesAnalysis(@PathVariable Long id) { return ApiResult.ok(svc.get(salesAnalysisMapper, id, "动销分析")); }
    @OperateLog(module = "商品管理", action = "CREATE", targetType = "SALES_ANALYSIS")
    @ApiOperation("新增动销") @PostMapping("/sales-analyses")
    public ApiResult<Void> createSalesAnalysis(@Valid @RequestBody ProductSalesAnalysis e) { svc.save(salesAnalysisMapper, e); return ApiResult.ok(); }
    @OperateLog(module = "商品管理", action = "UPDATE", targetType = "SALES_ANALYSIS", targetIdExpr = "#id")
    @ApiOperation("更新动销") @PutMapping("/sales-analyses/{id}")
    public ApiResult<Void> updateSalesAnalysis(@PathVariable Long id, @Valid @RequestBody ProductSalesAnalysis e) { e.setId(id); svc.update(salesAnalysisMapper, e); return ApiResult.ok(); }
    @OperateLog(module = "商品管理", action = "DELETE", targetType = "SALES_ANALYSIS", targetIdExpr = "#id")
    @ApiOperation("删除动销") @DeleteMapping("/sales-analyses/{id}")
    public ApiResult<Void> deleteSalesAnalysis(@PathVariable Long id) { svc.del(salesAnalysisMapper, id); return ApiResult.ok(); }

    // ===== 新品方案 =====
    @ApiOperation("新品方案分页") @GetMapping("/new-product-plans")
    public ApiResult<PageResult<NewProductPlan>> pageNewPlans(@Valid PageDTO dto) {
        IPage<NewProductPlan> r = svc.page(newProductPlanMapper, dto, new LambdaQueryWrapper<>()); return ApiResult.ok(PageResult.of(r)); }
    @ApiOperation("新品方案详情") @GetMapping("/new-product-plans/{id}")
    public ApiResult<NewProductPlan> getNewPlan(@PathVariable Long id) { return ApiResult.ok(svc.get(newProductPlanMapper, id, "新品方案")); }
    @OperateLog(module = "商品管理", action = "CREATE", targetType = "NEW_PRODUCT_PLAN")
    @ApiOperation("新增新品方案") @PostMapping("/new-product-plans")
    public ApiResult<Void> createNewPlan(@Valid @RequestBody NewProductPlan e) { svc.save(newProductPlanMapper, e); return ApiResult.ok(); }
    @OperateLog(module = "商品管理", action = "UPDATE", targetType = "NEW_PRODUCT_PLAN", targetIdExpr = "#id")
    @ApiOperation("更新新品方案") @PutMapping("/new-product-plans/{id}")
    public ApiResult<Void> updateNewPlan(@PathVariable Long id, @Valid @RequestBody NewProductPlan e) { e.setId(id); svc.update(newProductPlanMapper, e); return ApiResult.ok(); }
    @OperateLog(module = "商品管理", action = "DELETE", targetType = "NEW_PRODUCT_PLAN", targetIdExpr = "#id")
    @ApiOperation("删除新品方案") @DeleteMapping("/new-product-plans/{id}")
    public ApiResult<Void> deleteNewPlan(@PathVariable Long id) { svc.del(newProductPlanMapper, id); return ApiResult.ok(); }

    // ===== 促销方案 =====
    @ApiOperation("促销方案分页") @GetMapping("/promotion-plans")
    public ApiResult<PageResult<PromotionPlan>> pagePromotions(@Valid PageDTO dto) {
        IPage<PromotionPlan> r = svc.page(promotionPlanMapper, dto, new LambdaQueryWrapper<>()); return ApiResult.ok(PageResult.of(r)); }
    @ApiOperation("促销方案详情") @GetMapping("/promotion-plans/{id}")
    public ApiResult<PromotionPlan> getPromotion(@PathVariable Long id) { return ApiResult.ok(svc.get(promotionPlanMapper, id, "促销方案")); }
    @OperateLog(module = "商品管理", action = "CREATE", targetType = "PROMOTION_PLAN")
    @ApiOperation("新增促销方案") @PostMapping("/promotion-plans")
    public ApiResult<Void> createPromotion(@Valid @RequestBody PromotionPlan e) { svc.save(promotionPlanMapper, e); return ApiResult.ok(); }
    @OperateLog(module = "商品管理", action = "UPDATE", targetType = "PROMOTION_PLAN", targetIdExpr = "#id")
    @ApiOperation("更新促销方案") @PutMapping("/promotion-plans/{id}")
    public ApiResult<Void> updatePromotion(@PathVariable Long id, @Valid @RequestBody PromotionPlan e) { e.setId(id); svc.update(promotionPlanMapper, e); return ApiResult.ok(); }
    @OperateLog(module = "商品管理", action = "DELETE", targetType = "PROMOTION_PLAN", targetIdExpr = "#id")
    @ApiOperation("删除促销方案") @DeleteMapping("/promotion-plans/{id}")
    public ApiResult<Void> deletePromotion(@PathVariable Long id) { svc.del(promotionPlanMapper, id); return ApiResult.ok(); }

    // ===== 分类/品牌（Product.category=分类, Product.style=品牌） =====

    @GetMapping("/categories/tree")
    public ApiResult<java.util.List<java.util.Map<String, Object>>> categoryTree() {
        java.util.List<Product> all = productMapper.selectList(null);
        java.util.Map<String, java.util.Map<String, Object>> tree = new java.util.LinkedHashMap<>();
        for (Product p : all) {
            String cat = p.getCategory();
            if (cat == null) continue;
            String parent = cat.contains("黄金") ? "黄金" : cat.contains("钻石") ? "钻石"
                    : cat.contains("K金") ? "K金" : cat.contains("翡翠") ? "翡翠"
                    : cat.contains("珍珠") ? "珍珠" : cat.contains("铂金") ? "铂金"
                    : cat.contains("银") ? "银饰" : "其他";
            tree.putIfAbsent(parent, new java.util.LinkedHashMap<>());
            java.util.Map<String, Object> pNode = tree.get(parent);
            pNode.putIfAbsent("id", parent);
            pNode.putIfAbsent("name", parent);
            @SuppressWarnings("unchecked")
            java.util.List<java.util.Map<String, String>> children = (java.util.List<java.util.Map<String, String>>)
                    pNode.computeIfAbsent("children", k -> new java.util.ArrayList<>());
            java.util.Map<String, String> child = new java.util.LinkedHashMap<>();
            child.put("id", cat); child.put("name", cat);
            if (children.stream().noneMatch(c -> cat.equals(c.get("id")))) children.add(child);
        }
        return ApiResult.ok(new java.util.ArrayList<>(tree.values()));
    }

    @GetMapping("/categories")
    public ApiResult<java.util.List<java.util.Map<String, String>>> categories() {
        return ApiResult.ok(productMapper.selectList(null).stream()
                .filter(p -> p.getCategory() != null)
                .map(p -> { java.util.Map<String, String> m = new java.util.LinkedHashMap<>(); m.put("id", p.getCategory()); m.put("name", p.getCategory()); return m; })
                .collect(java.util.stream.Collectors.toCollection(() -> new java.util.TreeSet<>(java.util.Comparator.comparing(m -> m.get("id")))))
                .stream().collect(java.util.stream.Collectors.toList()));
    }

    @GetMapping("/brands")
    public ApiResult<PageResult<java.util.Map<String, String>>> brands(@Valid PageDTO dto, @RequestParam(required = false) String name) {
        java.util.List<java.util.Map<String, String>> all = productMapper.selectList(null).stream()
                .filter(p -> p.getStyle() != null)
                .map(p -> { java.util.Map<String, String> m = new java.util.LinkedHashMap<>(); m.put("id", p.getStyle()); m.put("name", p.getStyle()); return m; })
                .collect(java.util.stream.Collectors.toCollection(() -> new java.util.TreeSet<>(java.util.Comparator.comparing(m -> m.get("id")))))
                .stream().filter(b -> name == null || b.get("name").contains(name))
                .collect(java.util.stream.Collectors.toList());
        int start = (int) ((dto.getPageNum() - 1) * dto.getPageSize());
        java.util.List<java.util.Map<String, String>> page = all.stream().skip(start).limit(dto.getPageSize()).collect(java.util.stream.Collectors.toList());
        return ApiResult.ok(new PageResult<>(dto.getPageNum(), dto.getPageSize(), (long) all.size(), page));
    }

    @GetMapping("/brands/all")
    public ApiResult<java.util.List<java.util.Map<String, String>>> brandsAll() {
        return ApiResult.ok(productMapper.selectList(null).stream()
                .filter(p -> p.getStyle() != null && "on".equals(p.getStatus()))
                .map(p -> { java.util.Map<String, String> m = new java.util.LinkedHashMap<>(); m.put("id", p.getStyle()); m.put("name", p.getStyle()); return m; })
                .collect(java.util.stream.Collectors.toCollection(() -> new java.util.TreeSet<>(java.util.Comparator.comparing(m -> m.get("id")))))
                .stream().collect(java.util.stream.Collectors.toList()));
    }

    /** 填充 stock(最新盘点数量) 和 storeName */
    private void fillStockAndStoreName(java.util.List<Product> list) {
        if (list.isEmpty()) return;
        java.util.Set<Long> storeIds = new java.util.HashSet<>();
        java.util.Set<Long> productIds = new java.util.HashSet<>();
        for (Product p : list) { if (p.getStoreId() != null) storeIds.add(p.getStoreId()); productIds.add(p.getId()); }
        java.util.Map<Long, String> storeNameMap = new java.util.HashMap<>();
        if (!storeIds.isEmpty()) {
            for (com.zhubao.manage.module.organization.entity.Store s : storeMapper.selectBatchIds(storeIds))
                storeNameMap.put(s.getId(), s.getStoreName());
        }
        for (Product p : list) {
            if (p.getStoreId() != null) p.setStoreName(storeNameMap.get(p.getStoreId()));
            // stock 优先取最近一次盘点数，无盘点则用表内真实库存
            ProductInventoryCheck check = inventoryMapper.selectOne(
                    new LambdaQueryWrapper<ProductInventoryCheck>()
                            .eq(ProductInventoryCheck::getStoreId, p.getStoreId())
                            .orderByDesc(ProductInventoryCheck::getCheckDate).last("LIMIT 1"));
            if (check != null) {
                p.setStock(check.getTotalCheckedCount());
            } else if (p.getStock() == null) {
                p.setStock(0);
            }
        }
    }

    @OperateLog(module = "商品管理", action = "UPDATE", targetType = "PRODUCT", targetIdExpr = "#id")
    @ApiOperation("库存调整") @PutMapping("/{id}/adjust-stock")
    public ApiResult<Void> adjustStock(@PathVariable Long id, @RequestBody java.util.Map<String, Object> body) {
        Product p = productMapper.selectById(id);
        if (p == null) return ApiResult.fail("商品不存在");
        int delta = ((Number) body.get("delta")).intValue();
        int cur = p.getStock() != null ? p.getStock() : 0;
        if (cur + delta < 0) return ApiResult.fail("库存不足");
        p.setStock(cur + delta);
        productMapper.updateById(p);
        return ApiResult.ok();
    }

    @OperateLog(module = "商品管理", action = "TRANSFER", targetType = "PRODUCT")
    @ApiOperation("商品调拨") @PostMapping("/transfer")
    public ApiResult<Void> transfer(@RequestBody java.util.Map<String, Object> body) {
        Long productId = ((Number) body.get("productId")).longValue();
        Long toStoreId = ((Number) body.get("toStoreId")).longValue();
        int qty = ((Number) body.get("quantity")).intValue();
        Product from = productMapper.selectById(productId);
        if (from == null) return ApiResult.fail("商品不存在");
        int cur = from.getStock() != null ? from.getStock() : 0;
        if (qty > cur) return ApiResult.fail("调拨数量大于当前库存");
        if (qty == cur) {
            // 整件调拨：商品整体移入目标门店
            from.setStoreId(toStoreId);
        } else {
            // 部分调拨：源门店扣减库存
            from.setStock(cur - qty);
        }
        productMapper.updateById(from);
        return ApiResult.ok();
    }

    @ApiOperation("库存预警列表") @GetMapping("/inventory-warnings")
    public ApiResult<PageResult<java.util.Map<String, Object>>> inventoryWarnings(@Valid PageDTO dto,
            @RequestParam(required = false) String alertType,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String warehouse,
            @RequestParam(required = false) String status) {
        java.util.List<Product> all = svc.listFiltered();
        fillStockAndStoreName(all);
        java.util.List<java.util.Map<String, Object>> warnings = new ArrayList<>();
        for (Product p : all) {
            Integer stock = p.getStock() != null ? p.getStock() : 0;
            if (stock > 10) continue;
            java.util.Map<String, Object> m = new java.util.LinkedHashMap<>();
            m.put("id", p.getId().toString());
            m.put("alertType", stock <= 5 ? "shortage" : "warning");
            m.put("productCode", p.getProductCode());
            m.put("productName", p.getProductName());
            m.put("spec", p.getSize() != null ? p.getSize() : (p.getMaterial() != null ? p.getMaterial() : "-"));
            m.put("warehouse", p.getStoreName());
            m.put("currentQty", stock);
            m.put("safetyStock", 10);
            m.put("threshold", stock <= 5 ? "≤5" : "≤10");
            m.put("alertTime", p.getUpdatedAt() != null ? p.getUpdatedAt() : p.getCreatedAt());
            m.put("status", p.getWarningHandledAt() != null ? "handled" : "pending");
            m.put("handleTime", p.getWarningHandledAt());
            warnings.add(m);
        }
        if (StringUtils.isNotBlank(alertType)) {
            warnings.removeIf(w -> !alertType.equals(w.get("alertType")));
        }
        if (StringUtils.isNotBlank(keyword)) {
            String kw = keyword.toLowerCase();
            warnings.removeIf(w -> {
                String code = ((String) w.get("productCode")).toLowerCase();
                String name = ((String) w.get("productName")).toLowerCase();
                return !code.contains(kw) && !name.contains(kw);
            });
        }
        if (StringUtils.isNotBlank(warehouse)) {
            warnings.removeIf(w -> {
                String wh = (String) w.get("warehouse");
                return wh == null || !wh.contains(warehouse);
            });
        }
        if (StringUtils.isNotBlank(status)) {
            warnings.removeIf(w -> !status.equals(w.get("status")));
        }
        warnings.sort(Comparator.comparingInt(w -> (Integer) w.get("currentQty")));
        int start = (int) ((dto.getPageNum() - 1) * dto.getPageSize());
        java.util.List<java.util.Map<String, Object>> page = warnings.stream()
                .skip(start).limit(dto.getPageSize()).collect(Collectors.toList());
        return ApiResult.ok(new PageResult<>(dto.getPageNum(), dto.getPageSize(), (long) warnings.size(), page));
    }

    @ApiOperation("库存预警统计") @GetMapping("/inventory-warnings/stats")
    public ApiResult<java.util.Map<String, Object>> inventoryWarningStats() {
        java.util.List<Product> all = productMapper.selectList(null);
        fillStockAndStoreName(all);
        int shortage = 0, warning = 0, normal = 0;
        for (Product p : all) {
            Integer stock = p.getStock();
            if (stock == null || stock <= 5) shortage++;
            else if (stock <= 10) warning++;
            else normal++;
        }
        java.util.Map<String, Object> stats = new java.util.LinkedHashMap<>();
        stats.put("shortageCount", shortage);
        stats.put("warningCount", warning);
        stats.put("normalCount", normal);
        stats.put("expiringCount", 0);
        stats.put("transitTimeoutCount", 0);
        return ApiResult.ok(stats);
    }

    @OperateLog(module = "商品管理", action = "UPDATE", targetType = "PRODUCT", targetIdExpr = "#id")
    @ApiOperation("处理库存预警") @PutMapping("/inventory-warnings/{id}/handle")
    public ApiResult<Void> handleWarning(@PathVariable Long id, @RequestBody(required = false) java.util.Map<String, Object> body) {
        Product p = productMapper.selectById(id);
        if (p == null) return ApiResult.fail("商品不存在");
        if (body != null && body.get("delta") != null) {
            int delta = ((Number) body.get("delta")).intValue();
            p.setStock((p.getStock() != null ? p.getStock() : 0) + delta);
        }
        p.setWarningHandledAt(java.time.LocalDateTime.now());
        productMapper.updateById(p);
        return ApiResult.ok();
    }
}
