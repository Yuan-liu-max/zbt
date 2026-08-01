package com.zhubao.manage.module.purchase.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.zhubao.manage.common.dto.ApiResult;
import com.zhubao.manage.common.dto.PageDTO;
import com.zhubao.manage.common.dto.PageResult;
import com.zhubao.manage.module.purchase.entity.PurchaseItem;
import com.zhubao.manage.module.purchase.entity.PurchaseOrder;
import com.zhubao.manage.module.purchase.service.PurchaseService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.Map;

@Api(tags = "采购管理")
@RestController
@RequestMapping("/purchases")
@PreAuthorize("isAuthenticated()")
public class PurchaseController {

    private final PurchaseService purchaseService;

    public PurchaseController(PurchaseService ps) { this.purchaseService = ps; }

    @ApiOperation("采购单分页") @GetMapping
    public ApiResult<PageResult<PurchaseOrder>> list(@Valid PageDTO dto) {
        IPage<PurchaseOrder> p = purchaseService.page(dto); return ApiResult.ok(PageResult.of(p)); }

    @ApiOperation("采购单详情") @GetMapping("/{id}")
    public ApiResult<PurchaseOrder> detail(@PathVariable Long id) { return ApiResult.ok(purchaseService.detail(id)); }

    @ApiOperation("采购明细") @GetMapping("/{id}/items")
    public ApiResult<List<PurchaseItem>> items(@PathVariable Long id) { return ApiResult.ok(purchaseService.items(id)); }

    @ApiOperation("创建采购单") @PostMapping
    public ApiResult<PurchaseOrder> create(@Valid @RequestBody Map<String, Object> body) {
        PurchaseOrder order = new PurchaseOrder();
        order.setOrderNo((String) body.get("orderNo"));
        order.setStoreId(toLong(body.get("storeId")));
        order.setSupplierId(toLong(body.get("supplierId")));
        order.setTotalAmount(new java.math.BigDecimal(body.get("totalAmount").toString()));
        order.setRemark((String) body.get("remark"));
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> items = (List<Map<String, Object>>) body.get("items");
        List<PurchaseItem> itemList = items != null ? items.stream().map(m -> {
            PurchaseItem i = new PurchaseItem();
            i.setProductId(toLong(m.get("productId")));
            i.setProductName((String) m.get("productName"));
            i.setQuantity(((Number) m.get("quantity")).intValue());
            i.setPrice(new java.math.BigDecimal(m.get("price").toString()));
            return i;
        }).collect(java.util.stream.Collectors.toList()) : null;
        return ApiResult.ok(purchaseService.create(order, itemList));
    }

    @ApiOperation("更新采购单") @PutMapping("/{id}")
    public ApiResult<Void> update(@PathVariable Long id, @Valid @RequestBody PurchaseOrder o) { purchaseService.update(id, o); return ApiResult.ok(); }

    @ApiOperation("提交审核") @PutMapping("/{id}/submit")
    public ApiResult<Void> submit(@PathVariable Long id) { purchaseService.submit(id); return ApiResult.ok(); }

    @ApiOperation("审核通过") @PutMapping("/{id}/approve")
    public ApiResult<Void> approve(@PathVariable Long id) { purchaseService.approve(id); return ApiResult.ok(); }

    @ApiOperation("审核驳回") @PutMapping("/{id}/reject")
    public ApiResult<Void> reject(@PathVariable Long id) { purchaseService.reject(id); return ApiResult.ok(); }

    @ApiOperation("删除采购单") @DeleteMapping("/{id}")
    public ApiResult<Void> delete(@PathVariable Long id) { purchaseService.delete(id); return ApiResult.ok(); }

    private Long toLong(Object v) { return v instanceof Number ? ((Number) v).longValue() : null; }
}
