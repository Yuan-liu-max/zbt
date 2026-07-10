package com.zhubao.manage.module.sales.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
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
public class SalesController {

    private final SalesService salesService;

    public SalesController(SalesService salesService) { this.salesService = salesService; }

    @ApiOperation("销售分页") @GetMapping
    public ApiResult<PageResult<SalesRecord>> page(@Valid PageDTO dto) {
        IPage<SalesRecord> r = salesService.page(dto); return ApiResult.ok(PageResult.of(r)); }

    @ApiOperation("销售详情") @GetMapping("/{id}")
    public ApiResult<SalesRecord> detail(@PathVariable Long id) { return ApiResult.ok(salesService.detail(id)); }

    @ApiOperation("销售明细列表") @GetMapping("/{id}/items")
    public ApiResult<List<SalesItem>> items(@PathVariable Long id) { return ApiResult.ok(salesService.getItems(id)); }

    @ApiOperation("销售录入") @PostMapping
    public ApiResult<SalesRecord> create(@Valid @RequestBody SalesCreateDTO dto) { return ApiResult.ok(salesService.create(dto)); }

    @ApiOperation("销售审核") @PutMapping("/{id}/audit")
    public ApiResult<Void> audit(@PathVariable Long id, @RequestParam String auditStatus, @RequestParam(required = false) String comment) {
        salesService.audit(id, auditStatus, comment); return ApiResult.ok(); }

    @ApiOperation("员工指标") @GetMapping("/metrics/employee/{employeeId}")
    public ApiResult<Map<String, Object>> employeeMetrics(@PathVariable Long employeeId, @RequestParam String month) {
        return ApiResult.ok(salesService.employeeMetrics(employeeId, month)); }

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
}
