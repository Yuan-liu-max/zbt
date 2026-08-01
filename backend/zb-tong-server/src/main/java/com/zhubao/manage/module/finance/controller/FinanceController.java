package com.zhubao.manage.module.finance.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.zhubao.manage.common.dto.ApiResult;
import com.zhubao.manage.common.dto.PageDTO;
import com.zhubao.manage.common.dto.PageResult;
import com.zhubao.manage.module.finance.entity.Transaction;
import com.zhubao.manage.module.finance.service.FinanceService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Map;

@Api(tags = "财务管理")
@RestController
@RequestMapping("/transactions")
@PreAuthorize("isAuthenticated()")
public class FinanceController {

    private final FinanceService financeService;

    public FinanceController(FinanceService fs) { this.financeService = fs; }

    @ApiOperation("交易分页列表")
    @GetMapping
    public ApiResult<PageResult<Transaction>> list(@Valid PageDTO dto,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(required = false) String account,
            @RequestParam(required = false) String relatedObject,
            @RequestParam(required = false) String keyword) {
        IPage<Transaction> p = financeService.page(dto, type, startDate, endDate, account, relatedObject, keyword);
        return ApiResult.ok(PageResult.of(p));
    }

    @ApiOperation("交易汇总统计")
    @GetMapping("/stats")
    public ApiResult<Map<String, Object>> stats() { return ApiResult.ok(financeService.stats()); }

    @ApiOperation("交易详情") @GetMapping("/{id}")
    public ApiResult<Transaction> detail(@PathVariable Long id) { return ApiResult.ok(financeService.detail(id)); }

    @ApiOperation("新增交易") @PostMapping
    public ApiResult<Transaction> create(@Valid @RequestBody Transaction t) { return ApiResult.ok(financeService.create(t)); }

    @ApiOperation("更新交易") @PutMapping("/{id}")
    public ApiResult<Transaction> update(@PathVariable Long id, @Valid @RequestBody Transaction t) { return ApiResult.ok(financeService.update(id, t)); }

    @ApiOperation("删除交易") @DeleteMapping("/{id}")
    public ApiResult<Void> delete(@PathVariable Long id) { financeService.delete(id); return ApiResult.ok(); }
}
