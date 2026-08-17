package com.zhubao.manage.module.task.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.zhubao.manage.common.annotation.OperateLog;
import com.zhubao.manage.common.dto.ApiResult;
import com.zhubao.manage.common.dto.PageResult;
import com.zhubao.manage.common.interceptor.UserContextHolder;
import com.zhubao.manage.module.task.dto.*;
import com.zhubao.manage.module.task.entity.*;
import com.zhubao.manage.module.task.service.*;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@Api(tags = "任务管理")
@RestController
@RequestMapping("/tasks")
@org.springframework.security.access.prepost.PreAuthorize("isAuthenticated()")
public class TaskController {

    private final TaskService taskService;
    private final TaskSubmitService taskSubmitService;
    private final TaskAuditService taskAuditService;
    private final TaskReminderService taskReminderService;
    private final UserContextHolder userContextHolder;

    public TaskController(TaskService taskService, TaskSubmitService taskSubmitService,
                          TaskAuditService taskAuditService, TaskReminderService taskReminderService,
                          UserContextHolder userContextHolder) {
        this.taskService = taskService;
        this.taskSubmitService = taskSubmitService;
        this.taskAuditService = taskAuditService;
        this.taskReminderService = taskReminderService;
        this.userContextHolder = userContextHolder;
    }

    // ========== 任务管理 ==========

    @OperateLog(module = "任务管理", action = "CREATE", targetType = "TASK")
    @ApiOperation("批量生成任务")
    @PostMapping("/generate")
    public ApiResult<List<TaskInstance>> generate(@Valid @RequestBody TaskGenerateDTO dto) {
        return ApiResult.ok(taskService.generateTasks(dto.getTemplateId(), dto.getStoreIds()));
    }

    @OperateLog(module = "任务管理", action = "CREATE", targetType = "TASK")
    @ApiOperation("手动创建任务")
    @PostMapping
    public ApiResult<TaskInstance> create(@RequestBody TaskInstance task) {
        return ApiResult.ok(taskService.createTask(task));
    }

    @ApiOperation("任务分页列表（多维筛选）")
    @GetMapping
    public ApiResult<PageResult<TaskInstance>> page(@Valid TaskQueryDTO query) {
        IPage<TaskInstance> page = taskService.page(query);
        return ApiResult.ok(PageResult.of(page));
    }

    @ApiOperation("任务详情")
    @GetMapping("/{id}")
    public ApiResult<TaskInstance> detail(@PathVariable Long id) {
        return ApiResult.ok(taskService.detail(id));
    }

    @OperateLog(module = "任务管理", action = "CANCEL", targetType = "TASK", targetIdExpr = "#id")
    @ApiOperation("取消任务")
    @PutMapping("/{id}/cancel")
    public ApiResult<Void> cancel(@PathVariable Long id) {
        taskService.cancelTask(id);
        return ApiResult.ok();
    }

    @OperateLog(module = "任务管理", action = "VOID", targetType = "TASK", targetIdExpr = "#id")
    @ApiOperation("作废任务")
    @PutMapping("/{id}/void")
    public ApiResult<Void> voidTask(@PathVariable Long id) {
        taskService.voidTask(id);
        return ApiResult.ok();
    }

    @OperateLog(module = "任务管理", action = "UPDATE", targetType = "TASK", targetIdExpr = "#id")
    @ApiOperation("开始执行")
    @PutMapping("/{id}/start")
    public ApiResult<Void> start(@PathVariable Long id) {
        taskService.startTask(id);
        return ApiResult.ok();
    }

    @OperateLog(module = "任务管理", action = "UPDATE", targetType = "TASK", targetIdExpr = "#id")
    @ApiOperation("更新任务")
    @PutMapping("/{id}")
    public ApiResult<Void> update(@PathVariable Long id, @Valid @RequestBody TaskInstance task) {
        task.setId(id);
        taskService.updateTask(task);
        return ApiResult.ok();
    }

    // ========== 我的任务 ==========

    @ApiOperation("我的任务")
    @GetMapping("/my")
    public ApiResult<List<TaskInstance>> myTasks(@RequestParam(required = false) String status) {
        Long userId = userContextHolder.getUserId();
        return ApiResult.ok(taskService.getMyTasks(userId, status));
    }

    @ApiOperation("待审核任务")
    @GetMapping("/my-audit")
    public ApiResult<List<TaskInstance>> myAuditTasks() {
        return ApiResult.ok(taskService.getMyAuditTasks(userContextHolder.getUserId()));
    }

    // ========== 提交 ==========

    @OperateLog(module = "任务管理", action = "SUBMIT", targetType = "TASK")
    @ApiOperation("提交任务")
    @PostMapping("/submit")
    public ApiResult<TaskSubmission> submit(@Valid @RequestBody TaskSubmitDTO dto) {
        return ApiResult.ok(taskSubmitService.submit(dto));
    }

    @ApiOperation("查看最新提交")
    @GetMapping("/{id}/submission")
    public ApiResult<TaskSubmission> getSubmission(@PathVariable Long id) {
        return ApiResult.ok(taskSubmitService.getLatestSubmission(id));
    }

    // ========== 审核 ==========

    @OperateLog(module = "任务管理", action = "AUDIT", targetType = "TASK")
    @ApiOperation("审核任务")
    @PostMapping("/audit")
    public ApiResult<TaskAudit> audit(@Valid @RequestBody TaskAuditDTO dto) {
        return ApiResult.ok(taskAuditService.audit(dto));
    }

    @ApiOperation("审核记录")
    @GetMapping("/{id}/audit-history")
    public ApiResult<List<TaskAudit>> auditHistory(@PathVariable Long id) {
        return ApiResult.ok(taskAuditService.getAuditHistory(id));
    }

    // ========== 提醒 ==========

    @ApiOperation("扫描超时任务")
    @PostMapping("/scan-overdue")
    public ApiResult<Integer> scanOverdue() {
        return ApiResult.ok(taskReminderService.scanOverdueTasks());
    }
}
