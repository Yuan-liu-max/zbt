package com.zhubao.manage.module.product.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.zhubao.manage.common.dto.ApiResult;
import com.zhubao.manage.common.dto.PageDTO;
import com.zhubao.manage.common.dto.PageResult;
import com.zhubao.manage.module.product.entity.*;
import com.zhubao.manage.module.product.mapper.*;
import com.zhubao.manage.module.product.service.ProductService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@Api(tags = "货品管理")
@RestController
@RequestMapping("/products")
@org.springframework.security.access.prepost.PreAuthorize("isAuthenticated()")
public class ProductController {

    private final ProductService svc;
    private final ProductMapper productMapper;
    private final ProductInventoryCheckMapper inventoryMapper;
    private final ProductMaintenanceCheckMapper maintenanceMapper;
    private final ProductSalesAnalysisMapper salesAnalysisMapper;
    private final NewProductPlanMapper newProductPlanMapper;
    private final PromotionPlanMapper promotionPlanMapper;
    private final com.zhubao.manage.module.organization.mapper.StoreMapper storeMapper;

    public ProductController(ProductService svc, ProductMapper pm, ProductInventoryCheckMapper im,
                             ProductMaintenanceCheckMapper mm, ProductSalesAnalysisMapper sm,
                             NewProductPlanMapper nm, PromotionPlanMapper pm2,
                             com.zhubao.manage.module.organization.mapper.StoreMapper stm) {
        this.svc = svc; this.productMapper = pm; this.inventoryMapper = im;
        this.maintenanceMapper = mm; this.salesAnalysisMapper = sm;
        this.newProductPlanMapper = nm; this.promotionPlanMapper = pm2;
        this.storeMapper = stm;
    }

    // ===== 商品档案 =====
    @ApiOperation("商品列表（含角色过滤）") @GetMapping
    public ApiResult<List<Product>> listProducts() {
        List<Product> list = svc.listFiltered();
        fillStockAndStoreName(list);
        return ApiResult.ok(list);
    }
    @ApiOperation("商品分页") @GetMapping("/page")
    public ApiResult<PageResult<Product>> pageProducts(@Valid PageDTO dto) {
        IPage<Product> r = svc.page(productMapper, dto, new LambdaQueryWrapper<Product>().orderByDesc(Product::getCreatedAt));
        return ApiResult.ok(PageResult.of(r)); }
    @ApiOperation("商品详情（含角色过滤）") @GetMapping("/{id}")
    public ApiResult<Product> getProduct(@PathVariable Long id) { return ApiResult.ok(svc.getFiltered(id)); }
    @ApiOperation("新增商品") @PostMapping
    public ApiResult<Void> createProduct(@Valid @RequestBody Product e) { svc.save(productMapper, e); return ApiResult.ok(); }
    @ApiOperation("更新商品") @PutMapping("/{id}")
    public ApiResult<Void> updateProduct(@PathVariable Long id, @Valid @RequestBody Product e) { e.setId(id); svc.update(productMapper, e); return ApiResult.ok(); }
    @ApiOperation("删除商品") @DeleteMapping("/{id}")
    public ApiResult<Void> deleteProduct(@PathVariable Long id) { svc.del(productMapper, id); return ApiResult.ok(); }

    // ===== 盘点 =====
    @ApiOperation("盘点分页") @GetMapping("/inventory-checks")
    public ApiResult<PageResult<ProductInventoryCheck>> pageInventories(@Valid PageDTO dto) {
        IPage<ProductInventoryCheck> r = svc.page(inventoryMapper, dto, new LambdaQueryWrapper<>()); return ApiResult.ok(PageResult.of(r)); }
    @ApiOperation("盘点详情") @GetMapping("/inventory-checks/{id}")
    public ApiResult<ProductInventoryCheck> getInventory(@PathVariable Long id) { return ApiResult.ok(svc.get(inventoryMapper, id, "盘点")); }
    @ApiOperation("新增盘点") @PostMapping("/inventory-checks")
    public ApiResult<Void> createInventory(@Valid @RequestBody ProductInventoryCheck e) { svc.save(inventoryMapper, e); return ApiResult.ok(); }
    @ApiOperation("更新盘点") @PutMapping("/inventory-checks/{id}")
    public ApiResult<Void> updateInventory(@PathVariable Long id, @Valid @RequestBody ProductInventoryCheck e) { e.setId(id); svc.update(inventoryMapper, e); return ApiResult.ok(); }
    @ApiOperation("删除盘点") @DeleteMapping("/inventory-checks/{id}")
    public ApiResult<Void> deleteInventory(@PathVariable Long id) { svc.del(inventoryMapper, id); return ApiResult.ok(); }

    // ===== 养护 =====
    @ApiOperation("养护分页") @GetMapping("/maintenance-checks")
    public ApiResult<PageResult<ProductMaintenanceCheck>> pageMaintenances(@Valid PageDTO dto) {
        IPage<ProductMaintenanceCheck> r = svc.page(maintenanceMapper, dto, new LambdaQueryWrapper<>()); return ApiResult.ok(PageResult.of(r)); }
    @ApiOperation("养护详情") @GetMapping("/maintenance-checks/{id}")
    public ApiResult<ProductMaintenanceCheck> getMaintenance(@PathVariable Long id) { return ApiResult.ok(svc.get(maintenanceMapper, id, "养护")); }
    @ApiOperation("新增养护") @PostMapping("/maintenance-checks")
    public ApiResult<Void> createMaintenance(@Valid @RequestBody ProductMaintenanceCheck e) { svc.save(maintenanceMapper, e); return ApiResult.ok(); }
    @ApiOperation("更新养护") @PutMapping("/maintenance-checks/{id}")
    public ApiResult<Void> updateMaintenance(@PathVariable Long id, @Valid @RequestBody ProductMaintenanceCheck e) { e.setId(id); svc.update(maintenanceMapper, e); return ApiResult.ok(); }
    @ApiOperation("删除养护") @DeleteMapping("/maintenance-checks/{id}")
    public ApiResult<Void> deleteMaintenance(@PathVariable Long id) { svc.del(maintenanceMapper, id); return ApiResult.ok(); }

    // ===== 动销分析 =====
    @ApiOperation("动销分页") @GetMapping("/sales-analyses")
    public ApiResult<PageResult<ProductSalesAnalysis>> pageSalesAnalyses(@Valid PageDTO dto) {
        IPage<ProductSalesAnalysis> r = svc.page(salesAnalysisMapper, dto, new LambdaQueryWrapper<>()); return ApiResult.ok(PageResult.of(r)); }
    @ApiOperation("动销详情") @GetMapping("/sales-analyses/{id}")
    public ApiResult<ProductSalesAnalysis> getSalesAnalysis(@PathVariable Long id) { return ApiResult.ok(svc.get(salesAnalysisMapper, id, "动销分析")); }
    @ApiOperation("新增动销") @PostMapping("/sales-analyses")
    public ApiResult<Void> createSalesAnalysis(@Valid @RequestBody ProductSalesAnalysis e) { svc.save(salesAnalysisMapper, e); return ApiResult.ok(); }
    @ApiOperation("更新动销") @PutMapping("/sales-analyses/{id}")
    public ApiResult<Void> updateSalesAnalysis(@PathVariable Long id, @Valid @RequestBody ProductSalesAnalysis e) { e.setId(id); svc.update(salesAnalysisMapper, e); return ApiResult.ok(); }
    @ApiOperation("删除动销") @DeleteMapping("/sales-analyses/{id}")
    public ApiResult<Void> deleteSalesAnalysis(@PathVariable Long id) { svc.del(salesAnalysisMapper, id); return ApiResult.ok(); }

    // ===== 新品方案 =====
    @ApiOperation("新品方案分页") @GetMapping("/new-product-plans")
    public ApiResult<PageResult<NewProductPlan>> pageNewPlans(@Valid PageDTO dto) {
        IPage<NewProductPlan> r = svc.page(newProductPlanMapper, dto, new LambdaQueryWrapper<>()); return ApiResult.ok(PageResult.of(r)); }
    @ApiOperation("新品方案详情") @GetMapping("/new-product-plans/{id}")
    public ApiResult<NewProductPlan> getNewPlan(@PathVariable Long id) { return ApiResult.ok(svc.get(newProductPlanMapper, id, "新品方案")); }
    @ApiOperation("新增新品方案") @PostMapping("/new-product-plans")
    public ApiResult<Void> createNewPlan(@Valid @RequestBody NewProductPlan e) { svc.save(newProductPlanMapper, e); return ApiResult.ok(); }
    @ApiOperation("更新新品方案") @PutMapping("/new-product-plans/{id}")
    public ApiResult<Void> updateNewPlan(@PathVariable Long id, @Valid @RequestBody NewProductPlan e) { e.setId(id); svc.update(newProductPlanMapper, e); return ApiResult.ok(); }
    @ApiOperation("删除新品方案") @DeleteMapping("/new-product-plans/{id}")
    public ApiResult<Void> deleteNewPlan(@PathVariable Long id) { svc.del(newProductPlanMapper, id); return ApiResult.ok(); }

    // ===== 促销方案 =====
    @ApiOperation("促销方案分页") @GetMapping("/promotion-plans")
    public ApiResult<PageResult<PromotionPlan>> pagePromotions(@Valid PageDTO dto) {
        IPage<PromotionPlan> r = svc.page(promotionPlanMapper, dto, new LambdaQueryWrapper<>()); return ApiResult.ok(PageResult.of(r)); }
    @ApiOperation("促销方案详情") @GetMapping("/promotion-plans/{id}")
    public ApiResult<PromotionPlan> getPromotion(@PathVariable Long id) { return ApiResult.ok(svc.get(promotionPlanMapper, id, "促销方案")); }
    @ApiOperation("新增促销方案") @PostMapping("/promotion-plans")
    public ApiResult<Void> createPromotion(@Valid @RequestBody PromotionPlan e) { svc.save(promotionPlanMapper, e); return ApiResult.ok(); }
    @ApiOperation("更新促销方案") @PutMapping("/promotion-plans/{id}")
    public ApiResult<Void> updatePromotion(@PathVariable Long id, @Valid @RequestBody PromotionPlan e) { e.setId(id); svc.update(promotionPlanMapper, e); return ApiResult.ok(); }
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
            // stock 从最近一次盘点取 totalCheckedCount
            ProductInventoryCheck check = inventoryMapper.selectOne(
                    new LambdaQueryWrapper<ProductInventoryCheck>()
                            .eq(ProductInventoryCheck::getStoreId, p.getStoreId())
                            .orderByDesc(ProductInventoryCheck::getCheckDate).last("LIMIT 1"));
            p.setStock(check != null ? check.getTotalCheckedCount() : 0);
        }
    }

    @ApiOperation("库存调整") @PutMapping("/{id}/adjust-stock")
    public ApiResult<Void> adjustStock(@PathVariable Long id, @RequestBody java.util.Map<String, Object> body) {
        Product p = productMapper.selectById(id);
        if (p == null) return ApiResult.fail("商品不存在");
        int delta = ((Number) body.get("delta")).intValue();
        p.setStock((p.getStock() != null ? p.getStock() : 0) + delta);
        productMapper.updateById(p);
        return ApiResult.ok();
    }

    @ApiOperation("商品调拨") @PostMapping("/transfer")
    public ApiResult<Void> transfer(@RequestBody java.util.Map<String, Object> body) {
        Long productId = ((Number) body.get("productId")).longValue();
        Long toStoreId = ((Number) body.get("toStoreId")).longValue();
        int qty = ((Number) body.get("quantity")).intValue();
        Product from = productMapper.selectById(productId);
        if (from == null) return ApiResult.fail("商品不存在");
        from.setStock(from.getStock() - qty);
        productMapper.updateById(from);
        return ApiResult.ok();
    }
}
