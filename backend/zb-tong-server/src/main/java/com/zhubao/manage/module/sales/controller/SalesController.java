package com.zhubao.manage.module.sales.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.zhubao.manage.common.annotation.OperateLog;
import com.zhubao.manage.common.dto.ApiResult;
import com.zhubao.manage.common.dto.PageDTO;
import com.zhubao.manage.common.dto.PageResult;
import com.zhubao.manage.module.sales.dto.SalesCreateDTO;
import com.zhubao.manage.module.sales.entity.SalesItem;
import com.zhubao.manage.module.sales.entity.SalesRecord;
import com.zhubao.manage.module.sales.service.SalesService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.Map;

@Api(tags = "业绩管理")
@RestController
@RequestMapping("/sales")
@org.springframework.security.access.prepost.PreAuthorize("isAuthenticated()")
public class SalesController {

    private final SalesService salesService;

    public SalesController(SalesService salesService) { this.salesService = salesService; }

    @ApiOperation("销售分页（支持筛选）") @GetMapping
    public ApiResult<PageResult<SalesRecord>> page(@Valid PageDTO dto,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long storeId,
            @RequestParam(required = false) Long employeeId,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(required = false) String auditStatus) {
        IPage<SalesRecord> r = salesService.page(dto, keyword, storeId, employeeId, startDate, endDate, auditStatus); return ApiResult.ok(PageResult.of(r)); }

    @ApiOperation("销售详情") @GetMapping("/{id}")
    public ApiResult<SalesRecord> detail(@PathVariable Long id) { return ApiResult.ok(salesService.detail(id)); }

    @ApiOperation("销售明细列表") @GetMapping("/{id}/items")
    public ApiResult<List<SalesItem>> items(@PathVariable Long id) { return ApiResult.ok(salesService.getItems(id)); }

    @OperateLog(module = "业绩管理", action = "CREATE", targetType = "SALES")
    @ApiOperation("销售录入") @PostMapping
    public ApiResult<SalesRecord> create(@Valid @RequestBody SalesCreateDTO dto) { return ApiResult.ok(salesService.create(dto)); }

    @OperateLog(module = "业绩管理", action = "AUDIT", targetType = "SALES", targetIdExpr = "#id")
    @ApiOperation("销售审核") @PutMapping("/{id}/audit")
    public ApiResult<Void> audit(@PathVariable Long id, @RequestParam String auditStatus, @RequestParam(required = false) String comment) {
        salesService.audit(id, auditStatus, comment); return ApiResult.ok(); }

    @ApiOperation("员工指标") @GetMapping("/metrics/employee/{employeeId}")
    public ApiResult<Map<String, Object>> employeeMetrics(@PathVariable Long employeeId, @RequestParam String month) {
        return ApiResult.ok(salesService.employeeMetrics(employeeId, month)); }

    @ApiOperation("销售统计") @GetMapping("/stats")
    public ApiResult<Map<String, Object>> stats() {
        return ApiResult.ok(salesService.stats());
    }

    @ApiOperation("门店指标") @GetMapping("/metrics/store/{storeId}")
    public ApiResult<Map<String, Object>> storeMetrics(@PathVariable Long storeId, @RequestParam String month) {
        return ApiResult.ok(salesService.storeMetrics(storeId, month)); }

    @ApiOperation("员工排行") @GetMapping("/ranking/employees")
    public ApiResult<List<Map<String, Object>>> employeeRanking(@RequestParam String month, @RequestParam(defaultValue = "10") int topN) {
        return ApiResult.ok(salesService.employeeRanking(month, topN)); }

    @ApiOperation("门店排行") @GetMapping("/ranking/stores")
    public ApiResult<List<Map<String, Object>>> storeRanking(@RequestParam String month) {
        return ApiResult.ok(salesService.storeRanking(month)); }

    @ApiOperation("品类结构") @GetMapping("/category-structure")
    public ApiResult<List<Map<String, Object>>> categoryStructure(@RequestParam String month, @RequestParam(required = false) Long storeId) {
        return ApiResult.ok(salesService.categoryStructure(month, storeId)); }

    @OperateLog(module = "业绩管理", action = "UPDATE", targetType = "SALES", targetIdExpr = "#id")
    @ApiOperation("更新销售记录") @PutMapping("/{id}")
    public ApiResult<Void> update(@PathVariable Long id, @Valid @RequestBody SalesRecord record) {
        salesService.update(id, record); return ApiResult.ok(); }

    @OperateLog(module = "业绩管理", action = "DELETE", targetType = "SALES", targetIdExpr = "#id")
    @ApiOperation("删除销售记录") @DeleteMapping("/{id}")
    public ApiResult<Void> delete(@PathVariable Long id) {
        salesService.delete(id); return ApiResult.ok(); }
}
