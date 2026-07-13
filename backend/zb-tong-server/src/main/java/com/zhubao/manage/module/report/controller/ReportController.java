package com.zhubao.manage.module.report.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.zhubao.manage.common.dto.ApiResult;
import com.zhubao.manage.common.dto.PageDTO;
import com.zhubao.manage.common.dto.PageResult;
import com.zhubao.manage.common.interceptor.UserContext;
import com.zhubao.manage.common.interceptor.UserContextHolder;
import com.zhubao.manage.module.report.entity.StoreMonthlyScore;
import com.zhubao.manage.module.report.service.ReportService;
import com.zhubao.manage.module.report.service.ScoreCalcService;
import com.zhubao.manage.module.role.entity.Role;
import com.zhubao.manage.module.role.entity.UserRole;
import com.zhubao.manage.module.role.mapper.RoleMapper;
import com.zhubao.manage.module.role.mapper.UserRoleMapper;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Api(tags = "报表中心")
@RestController
@RequestMapping("/reports")
@org.springframework.security.access.prepost.PreAuthorize("hasAnyRole('ADMIN','HQ')")
public class ReportController {

    private final ReportService reportService;
    private final ScoreCalcService scoreCalcService;
    private final UserContextHolder userContextHolder;
    private final UserRoleMapper userRoleMapper;
    private final RoleMapper roleMapper;

    public ReportController(ReportService rs, ScoreCalcService scs, UserContextHolder uc,
                            UserRoleMapper urm, RoleMapper rm) {
        this.reportService = rs; this.scoreCalcService = scs; this.userContextHolder = uc;
        this.userRoleMapper = urm; this.roleMapper = rm;
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

    /**
     * 数据驾驶舱 —— role 从数据库查询，不接受外部传参
     * storeId 从 UserContext 取，天然绑定当前用户门店
     */
    @ApiOperation("数据驾驶舱") @GetMapping("/dashboard")
    public ApiResult<Map<String, Object>> dashboard() {
        UserContext ctx = userContextHolder.get();
        Long userId = ctx != null ? ctx.getUserId() : null;
        Long storeId = ctx != null ? ctx.getStoreId() : null;
        String role = resolveUserRole(userId);
        return ApiResult.ok(reportService.dashboard(role, userId, storeId));
    }

    /** 从数据库查出当前用户的最高优先级角色码 */
    private String resolveUserRole(Long userId) {
        if (userId == null) return "STORE";
        try {
            List<Long> roleIds = userRoleMapper.selectList(
                    new LambdaQueryWrapper<UserRole>().eq(UserRole::getUserId, userId))
                    .stream().map(UserRole::getRoleId).collect(Collectors.toList());
            if (roleIds.isEmpty()) return "STORE";
            List<Role> roles = roleMapper.selectBatchIds(roleIds);
            return roles.stream()
                    .map(Role::getRoleCode)
                    .filter(r -> r != null)
                    .findFirst().orElse("STORE");
        } catch (Exception e) { return "STORE"; }
    }

    @ApiOperation("Excel导出门店评分") @GetMapping("/scores/export")
    public ApiResult<String> exportScores(@RequestParam String month) {
        // TODO: EasyExcel 导出实现
        return ApiResult.ok("TODO: 实现 Excel 导出 (" + month + ")");
    }
}
