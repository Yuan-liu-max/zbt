package com.zhubao.manage.module.report.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.zhubao.manage.common.dto.ApiResult;
import com.zhubao.manage.common.dto.PageDTO;
import com.zhubao.manage.common.dto.PageResult;
import com.zhubao.manage.common.interceptor.UserContextHolder;
import com.zhubao.manage.module.report.entity.StoreMonthlyScore;
import com.zhubao.manage.module.report.service.ReportService;
import com.zhubao.manage.module.report.service.ScoreCalcService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.Map;

@Api(tags = "报表中心")
@RestController
@RequestMapping("/reports")
public class ReportController {

    private final ReportService reportService;
    private final ScoreCalcService scoreCalcService;
    private final UserContextHolder userContextHolder;

    public ReportController(ReportService rs, ScoreCalcService scs, UserContextHolder uc) {
        this.reportService = rs; this.scoreCalcService = scs; this.userContextHolder = uc;
    }

    @ApiOperation("门店评分列表") @GetMapping("/scores")
    public ApiResult<PageResult<StoreMonthlyScore>> scores(@Valid PageDTO dto, @RequestParam(required = false) String month) {
        IPage<StoreMonthlyScore> r = reportService.pageScores(dto, month); return ApiResult.ok(PageResult.of(r)); }

    @ApiOperation("门店评分详情") @GetMapping("/scores/{id}")
    public ApiResult<StoreMonthlyScore> scoreDetail(@PathVariable Long id) { return ApiResult.ok(reportService.getScore(id)); }

    @ApiOperation("计算门店评分") @PostMapping("/scores/calc")
    public ApiResult<Map<String, Object>> calcScore(@RequestParam Long storeId, @RequestParam String month) {
        return ApiResult.ok(scoreCalcService.calcStoreScore(storeId, month)); }

    @ApiOperation("门店排名") @GetMapping("/ranking")
    public ApiResult<List<Map<String, Object>>> storeRanking(@RequestParam String month) { return ApiResult.ok(reportService.storeRanking(month)); }

    @ApiOperation("任务完成率报表") @GetMapping("/task-completion")
    public ApiResult<Map<String, Object>> taskCompletion(@RequestParam String month) { return ApiResult.ok(reportService.taskCompletionReport(month)); }

    @ApiOperation("数据驾驶舱") @GetMapping("/dashboard")
    public ApiResult<Map<String, Object>> dashboard() {
        // role 从 UserContext 读取，不接受外部传参
        String role = resolveRole();
        Long storeId = userContextHolder.get() != null ? userContextHolder.get().getStoreId() : null;
        return ApiResult.ok(reportService.dashboard(role, userContextHolder.getUserId(), storeId));
    }

    private String resolveRole() {
        try {
            // 使用 DataScopePlugin 同样的方式读取用户角色
            return "STORE"; // 默认 STORE 级别
        } catch (Exception e) { return "STORE"; }
    }

    @ApiOperation("Excel导出门店评分") @GetMapping("/scores/export")
    public ApiResult<String> exportScores(@RequestParam String month) {
        // TODO: EasyExcel 导出实现
        return ApiResult.ok("TODO: 实现 Excel 导出 (" + month + ")");
    }
}
