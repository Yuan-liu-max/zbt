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
import java.util.Collections;
import java.util.List;

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

    public ProductController(ProductService svc, ProductMapper pm, ProductInventoryCheckMapper im,
                             ProductMaintenanceCheckMapper mm, ProductSalesAnalysisMapper sm,
                             NewProductPlanMapper nm, PromotionPlanMapper pm2) {
        this.svc = svc; this.productMapper = pm; this.inventoryMapper = im;
        this.maintenanceMapper = mm; this.salesAnalysisMapper = sm;
        this.newProductPlanMapper = nm; this.promotionPlanMapper = pm2;
    }

    // ===== 商品档案 =====
    @ApiOperation("商品列表") @GetMapping
    public ApiResult<List<Product>> listProducts() {
        return ApiResult.ok(svc.all(productMapper, new LambdaQueryWrapper<Product>().orderByDesc(Product::getCreatedAt))); }
    @ApiOperation("商品分页") @GetMapping("/page")
    public ApiResult<PageResult<Product>> pageProducts(PageDTO dto) {
        IPage<Product> r = svc.page(productMapper, dto, new LambdaQueryWrapper<Product>().orderByDesc(Product::getCreatedAt));
        return ApiResult.ok(PageResult.of(r)); }
    @ApiOperation("商品详情") @GetMapping("/{id}")
    public ApiResult<Product> getProduct(@PathVariable Long id) {
        return ApiResult.ok(svc.get(productMapper, id, "商品")); }
    @ApiOperation("新增商品") @PostMapping
    public ApiResult<Void> createProduct(@Valid @RequestBody Product e) { svc.save(productMapper, e); return ApiResult.ok(); }
    @ApiOperation("更新商品") @PutMapping("/{id}")
    public ApiResult<Void> updateProduct(@PathVariable Long id, @Valid @RequestBody Product e) { e.setId(id); svc.update(productMapper, e); return ApiResult.ok(); }
    @ApiOperation("删除商品") @DeleteMapping("/{id}")
    public ApiResult<Void> deleteProduct(@PathVariable Long id) { svc.del(productMapper, id); return ApiResult.ok(); }

    // ===== 盘点 =====
    @ApiOperation("盘点列表") @GetMapping("/inventory-checks")
    public ApiResult<List<ProductInventoryCheck>> listInventories() { return ApiResult.ok(svc.all(inventoryMapper, new LambdaQueryWrapper<>())); }
    @ApiOperation("新增盘点") @PostMapping("/inventory-checks")
    public ApiResult<Void> createInventory(@RequestBody ProductInventoryCheck e) { svc.save(inventoryMapper, e); return ApiResult.ok(); }

    // ===== 养护 =====
    @ApiOperation("养护列表") @GetMapping("/maintenance-checks")
    public ApiResult<List<ProductMaintenanceCheck>> listMaintenances() { return ApiResult.ok(svc.all(maintenanceMapper, new LambdaQueryWrapper<>())); }
    @ApiOperation("新增养护") @PostMapping("/maintenance-checks")
    public ApiResult<Void> createMaintenance(@RequestBody ProductMaintenanceCheck e) { svc.save(maintenanceMapper, e); return ApiResult.ok(); }

    // ===== 动销分析 =====
    @ApiOperation("动销列表") @GetMapping("/sales-analyses")
    public ApiResult<List<ProductSalesAnalysis>> listSalesAnalyses() { return ApiResult.ok(svc.all(salesAnalysisMapper, new LambdaQueryWrapper<>())); }
    @ApiOperation("新增动销") @PostMapping("/sales-analyses")
    public ApiResult<Void> createSalesAnalysis(@RequestBody ProductSalesAnalysis e) { svc.save(salesAnalysisMapper, e); return ApiResult.ok(); }

    // ===== 新品方案 =====
    @ApiOperation("新品方案列表") @GetMapping("/new-product-plans")
    public ApiResult<List<NewProductPlan>> listNewPlans() { return ApiResult.ok(svc.all(newProductPlanMapper, new LambdaQueryWrapper<>())); }
    @ApiOperation("新增新品方案") @PostMapping("/new-product-plans")
    public ApiResult<Void> createNewPlan(@RequestBody NewProductPlan e) { svc.save(newProductPlanMapper, e); return ApiResult.ok(); }

    // ===== 促销方案 =====
    @ApiOperation("促销方案列表") @GetMapping("/promotion-plans")
    public ApiResult<List<PromotionPlan>> listPromotions() { return ApiResult.ok(svc.all(promotionPlanMapper, new LambdaQueryWrapper<>())); }
    @ApiOperation("新增促销方案") @PostMapping("/promotion-plans")
    public ApiResult<Void> createPromotion(@RequestBody PromotionPlan e) { svc.save(promotionPlanMapper, e); return ApiResult.ok(); }
}
