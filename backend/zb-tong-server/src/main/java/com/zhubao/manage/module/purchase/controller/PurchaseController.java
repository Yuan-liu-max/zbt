package com.zhubao.manage.module.purchase.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.zhubao.manage.common.annotation.OperateLog;
import com.zhubao.manage.common.dto.ApiResult;
import com.zhubao.manage.common.dto.PageResult;
import com.zhubao.manage.module.purchase.dto.PurchaseCreateDTO;
import com.zhubao.manage.module.purchase.dto.PurchaseQueryDTO;
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
    public ApiResult<PageResult<PurchaseOrder>> list(@Valid PurchaseQueryDTO dto) {
        IPage<PurchaseOrder> p = purchaseService.page(dto); return ApiResult.ok(PageResult.of(p)); }

    @ApiOperation("采购单详情") @GetMapping("/{id}")
    public ApiResult<PurchaseOrder> detail(@PathVariable Long id) { return ApiResult.ok(purchaseService.detail(id)); }

    @ApiOperation("采购明细") @GetMapping("/{id}/items")
    public ApiResult<List<PurchaseItem>> items(@PathVariable Long id) { return ApiResult.ok(purchaseService.items(id)); }

    @OperateLog(module = "采购管理", action = "CREATE", targetType = "PURCHASE")
    @ApiOperation("创建采购单") @PostMapping
    public ApiResult<PurchaseOrder> create(@Valid @RequestBody PurchaseCreateDTO dto) {
        PurchaseOrder order = new PurchaseOrder();
        order.setOrderNo(dto.getOrderNo());
        order.setStoreId(dto.getStoreId());
        order.setSupplierId(dto.getSupplierId());
        order.setTotalAmount(dto.getTotalAmount());
        order.setRemark(dto.getRemark());
        List<PurchaseItem> items = dto.getItems();
        if (items != null) {
            for (PurchaseItem item : items) {
                item.setOrderId(null);
            }
        }
        return ApiResult.ok(purchaseService.create(order, items));
    }

    @OperateLog(module = "采购管理", action = "UPDATE", targetType = "PURCHASE", targetIdExpr = "#id")
    @ApiOperation("更新采购单") @PutMapping("/{id}")
    public ApiResult<Void> update(@PathVariable Long id, @Valid @RequestBody PurchaseOrder o) { purchaseService.update(id, o); return ApiResult.ok(); }

    @OperateLog(module = "采购管理", action = "SUBMIT", targetType = "PURCHASE", targetIdExpr = "#id")
    @ApiOperation("提交审核") @PutMapping("/{id}/submit")
    public ApiResult<Void> submit(@PathVariable Long id) { purchaseService.submit(id); return ApiResult.ok(); }

    @OperateLog(module = "采购管理", action = "APPROVE", targetType = "PURCHASE", targetIdExpr = "#id")
    @ApiOperation("审核通过") @PutMapping("/{id}/approve")
    public ApiResult<Void> approve(@PathVariable Long id) { purchaseService.approve(id); return ApiResult.ok(); }

    @OperateLog(module = "采购管理", action = "REJECT", targetType = "PURCHASE", targetIdExpr = "#id")
    @ApiOperation("审核驳回") @PutMapping("/{id}/reject")
    public ApiResult<Void> reject(@PathVariable Long id, @RequestBody(required = false) Map<String, Object> body) {
        String reason = body != null && body.get("reason") != null ? body.get("reason").toString() : null;
        purchaseService.reject(id, reason); return ApiResult.ok(); }

    @OperateLog(module = "采购管理", action = "CANCEL", targetType = "PURCHASE", targetIdExpr = "#id")
    @ApiOperation("取消采购单") @PutMapping("/{id}/cancel")
    public ApiResult<Void> cancel(@PathVariable Long id) { purchaseService.cancel(id); return ApiResult.ok(); }

    @OperateLog(module = "采购管理", action = "DELETE", targetType = "PURCHASE", targetIdExpr = "#id")
    @ApiOperation("删除采购单") @DeleteMapping("/{id}")
    public ApiResult<Void> delete(@PathVariable Long id) { purchaseService.delete(id); return ApiResult.ok(); }

    // ===== 采购明细管理 =====

    @OperateLog(module = "采购管理", action = "CREATE", targetType = "PURCHASE_ITEM", targetIdExpr = "#id")
    @ApiOperation("新增采购明细") @PostMapping("/{id}/items")
    public ApiResult<PurchaseItem> createItem(@PathVariable Long id, @Valid @RequestBody PurchaseItem item) {
        item.setOrderId(id); return ApiResult.ok(purchaseService.createItem(item)); }

    @OperateLog(module = "采购管理", action = "DELETE", targetType = "PURCHASE_ITEM", targetIdExpr = "#itemId")
    @ApiOperation("删除采购明细") @DeleteMapping("/{id}/items/{itemId}")
    public ApiResult<Void> deleteItem(@PathVariable Long id, @PathVariable Long itemId) {
        purchaseService.deleteItem(id, itemId); return ApiResult.ok(); }

}
