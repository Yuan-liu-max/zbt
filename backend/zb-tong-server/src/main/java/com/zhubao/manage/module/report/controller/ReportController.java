package com.zhubao.manage.module.report.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhubao.manage.common.dto.ApiResult;
import com.zhubao.manage.common.dto.PageDTO;
import com.zhubao.manage.common.dto.PageResult;
import com.zhubao.manage.common.interceptor.UserContext;
import com.zhubao.manage.common.interceptor.UserContextHolder;
import com.zhubao.manage.module.report.entity.OperateLog;
import com.zhubao.manage.module.report.entity.StoreMonthlyScore;
import com.zhubao.manage.module.report.mapper.OperateLogMapper;
import com.zhubao.manage.module.report.service.ReportService;
import com.zhubao.manage.module.report.service.ScoreCalcService;
import com.zhubao.manage.module.role.entity.Role;
import com.zhubao.manage.module.role.entity.UserRole;
import com.zhubao.manage.module.role.mapper.RoleMapper;
import com.zhubao.manage.module.role.mapper.UserRoleMapper;
import com.zhubao.manage.module.user.entity.User;
import com.zhubao.manage.module.user.mapper.UserMapper;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Api(tags = "报表中心")
@RestController
@RequestMapping("/reports")
public class ReportController {

    private final ReportService reportService;
    private final ScoreCalcService scoreCalcService;
    private final UserContextHolder userContextHolder;
    private final UserRoleMapper userRoleMapper;
    private final RoleMapper roleMapper;
    private final OperateLogMapper operateLogMapper;
    private final UserMapper userMapper;

    public ReportController(ReportService rs, ScoreCalcService scs, UserContextHolder uc,
                            UserRoleMapper urm, RoleMapper rm, OperateLogMapper olm, UserMapper um) {
        this.reportService = rs; this.scoreCalcService = scs; this.userContextHolder = uc;
        this.userRoleMapper = urm; this.roleMapper = rm; this.operateLogMapper = olm;
        this.userMapper = um;
    }

    @ApiOperation("门店评分列表") @GetMapping("/scores")
    @org.springframework.security.access.prepost.PreAuthorize("hasAnyRole('ADMIN','HQ')")
    public ApiResult<PageResult<StoreMonthlyScore>> scores(@Valid PageDTO dto, @RequestParam(required = false) String month) {
        IPage<StoreMonthlyScore> r = reportService.pageScores(dto, month); return ApiResult.ok(PageResult.of(r)); }

    @ApiOperation("门店评分详情") @GetMapping("/scores/{id}")
    @org.springframework.security.access.prepost.PreAuthorize("hasAnyRole('ADMIN','HQ')")
    public ApiResult<StoreMonthlyScore> scoreDetail(@PathVariable Long id) { return ApiResult.ok(reportService.getScore(id)); }

    @ApiOperation("计算门店评分") @PostMapping("/scores/calc")
    @org.springframework.security.access.prepost.PreAuthorize("hasAnyRole('ADMIN','HQ')")
    public ApiResult<Map<String, Object>> calcScore(@RequestParam Long storeId, @RequestParam String month) {
        return ApiResult.ok(scoreCalcService.calcStoreScore(storeId, month)); }

    @ApiOperation("排行榜（product/store/employee）") @GetMapping("/ranking")
    @org.springframework.security.access.prepost.PreAuthorize("isAuthenticated()")
    public ApiResult<List<Map<String, Object>>> ranking(
            @RequestParam(defaultValue = "product") String type,
            @RequestParam(required = false) String month) {
        UserContext ctx = userContextHolder.get();
        Long storeId = ctx != null ? ctx.getStoreId() : null;
        return ApiResult.ok(reportService.ranking(type, month, storeId));
    }

    @ApiOperation("门店评分排名（旧接口兼容）") @GetMapping("/ranking/store")
    @org.springframework.security.access.prepost.PreAuthorize("hasAnyRole('ADMIN','HQ')")
    public ApiResult<List<Map<String, Object>>> storeRanking(@RequestParam String month) { return ApiResult.ok(reportService.storeRanking(month)); }

    @ApiOperation("任务完成率报表") @GetMapping("/task-completion")
    @org.springframework.security.access.prepost.PreAuthorize("hasAnyRole('ADMIN','HQ')")
    public ApiResult<Map<String, Object>> taskCompletion(@RequestParam String month) { return ApiResult.ok(reportService.taskCompletionReport(month)); }

    /**
     * 数据驾驶舱 —— role 从数据库查询，不接受外部传参
     * storeId 从 UserContext 取，天然绑定当前用户门店
     * 所有已登录用户可访问（驾驶舱是各角色落地页）
     */
    @ApiOperation("数据驾驶舱（所有已登录用户可访问）")
    @GetMapping("/dashboard")
    @org.springframework.security.access.prepost.PreAuthorize("isAuthenticated()")
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
    @org.springframework.security.access.prepost.PreAuthorize("hasAnyRole('ADMIN','HQ')")
    public ApiResult<String> exportScores(@RequestParam String month) {
        return ApiResult.fail("导出功能开发中，敬请期待");
    }

    @ApiOperation("操作日志分页") @GetMapping("/operate-logs")
    @org.springframework.security.access.prepost.PreAuthorize("hasAnyRole('ADMIN','HQ')")
    public ApiResult<PageResult<OperateLog>> operateLogs(@Valid PageDTO dto,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String operator,
            @RequestParam(required = false) String module,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        LambdaQueryWrapper<OperateLog> w = buildOperateLogWrapper(keyword, operator, module, startDate, endDate);
        IPage<OperateLog> page = operateLogMapper.selectPage(new Page<>(dto.getPageNum(), dto.getPageSize()), w);
        fillOperatorNames(page.getRecords());
        return ApiResult.ok(PageResult.of(page));
    }

    @ApiOperation("导出操作日志CSV") @GetMapping("/operate-logs/export")
    @org.springframework.security.access.prepost.PreAuthorize("hasAnyRole('ADMIN','HQ')")
    public void exportOperateLogs(HttpServletResponse response,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String operator,
            @RequestParam(required = false) String module,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) throws IOException {
        List<OperateLog> logs = operateLogMapper.selectList(buildOperateLogWrapper(keyword, operator, module, startDate, endDate));
        fillOperatorNames(logs);
        response.setContentType("text/csv;charset=UTF-8");
        response.setCharacterEncoding("UTF-8");
        response.setHeader("Content-Disposition", "attachment; filename=operate-logs-" + System.currentTimeMillis() + ".csv");
        StringBuilder sb = new StringBuilder("\uFEFF");
        sb.append("日志时间,操作人员,操作模块,操作类型,操作对象,目标ID,IP地址,请求参数\n");
        for (OperateLog l : logs) {
            sb.append(escapeCsv(l.getCreatedAt() == null ? "" : l.getCreatedAt().toString())).append(',')
              .append(escapeCsv(l.getOperatorName())).append(',')
              .append(escapeCsv(l.getModule())).append(',')
              .append(escapeCsv(l.getAction())).append(',')
              .append(escapeCsv(l.getTargetType())).append(',')
              .append(escapeCsv(l.getTargetId() == null ? "" : l.getTargetId().toString())).append(',')
              .append(escapeCsv(l.getRequestIp())).append(',')
              .append(escapeCsv(l.getRequestParams())).append('\n');
        }
        response.getWriter().write(sb.toString());
        response.getWriter().flush();
    }

    /** 操作日志通用筛选条件（分页列表与导出共用） */
    private LambdaQueryWrapper<OperateLog> buildOperateLogWrapper(String keyword, String operator,
            String module, String startDate, String endDate) {
        LambdaQueryWrapper<OperateLog> w = new LambdaQueryWrapper<>();
        if (StringUtils.isNotBlank(keyword)) {
            w.and(x -> x.like(OperateLog::getModule, keyword)
                    .or().like(OperateLog::getAction, keyword)
                    .or().like(OperateLog::getTargetType, keyword)
                    .or().like(OperateLog::getRequestParams, keyword));
        }
        if (StringUtils.isNotBlank(operator)) {
            // operator 按姓名模糊匹配：先查出匹配的用户ID，再按 operatorId 过滤
            List<Long> userIds = userMapper.selectList(
                    new LambdaQueryWrapper<User>().like(User::getRealName, operator))
                    .stream().map(User::getId).collect(Collectors.toList());
            if (userIds.isEmpty()) {
                w.eq(OperateLog::getOperatorId, -1L);
            } else {
                w.in(OperateLog::getOperatorId, userIds);
            }
        }
        if (StringUtils.isNotBlank(module)) w.eq(OperateLog::getModule, module);
        if (StringUtils.isNotBlank(startDate)) w.ge(OperateLog::getCreatedAt, startDate);
        if (StringUtils.isNotBlank(endDate)) w.le(OperateLog::getCreatedAt, endDate);
        w.orderByDesc(OperateLog::getCreatedAt);
        return w;
    }

    /** 批量填充 operatorName（transient 字段，不落库） */
    private void fillOperatorNames(List<OperateLog> logs) {
        if (logs == null || logs.isEmpty()) return;
        Set<Long> operatorIds = logs.stream()
                .map(OperateLog::getOperatorId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        if (operatorIds.isEmpty()) return;
        Map<Long, String> nameMap = userMapper.selectBatchIds(operatorIds)
                .stream().collect(Collectors.toMap(User::getId,
                        u -> StringUtils.isNotBlank(u.getRealName()) ? u.getRealName() : u.getUsername()));
        for (OperateLog l : logs) {
            l.setOperatorName(nameMap.get(l.getOperatorId()));
        }
    }

    private String escapeCsv(String s) {
        if (s == null) return "";
        return "\"" + s.replace("\"", "\"\"") + "\"";
    }

    @ApiOperation("批量删除操作日志") @DeleteMapping("/operate-logs")
    @org.springframework.security.access.prepost.PreAuthorize("hasAnyRole('ADMIN','HQ')")
    public ApiResult<Void> deleteOperateLogs(@RequestBody Map<String, Object> body) {
        @SuppressWarnings("unchecked")
        List<Object> rawIds = (List<Object>) body.get("ids");
        if (rawIds == null || rawIds.isEmpty()) {
            return ApiResult.fail("ids 不能为空");
        }
        List<Long> ids = rawIds.stream()
                .map(o -> o instanceof Number ? ((Number) o).longValue() : Long.valueOf(o.toString()))
                .collect(Collectors.toList());
        operateLogMapper.deleteBatchIds(ids);
        return ApiResult.ok();
    }
}
