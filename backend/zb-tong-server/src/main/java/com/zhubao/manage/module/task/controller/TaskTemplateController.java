package com.zhubao.manage.module.task.controller;

import com.zhubao.manage.common.annotation.OperateLog;
import com.zhubao.manage.common.dto.ApiResult;
import com.zhubao.manage.common.dto.PageDTO;
import com.zhubao.manage.common.dto.PageResult;
import com.zhubao.manage.module.task.entity.TaskTemplate;
import com.zhubao.manage.module.task.service.TaskTemplateService;
import javax.validation.Valid;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Api(tags = "任务模板管理")
@RestController
@RequestMapping("/task-templates")
@org.springframework.security.access.prepost.PreAuthorize("hasAnyRole('ADMIN','HQ')")
public class TaskTemplateController {

    private final TaskTemplateService taskTemplateService;

    public TaskTemplateController(TaskTemplateService taskTemplateService) {
        this.taskTemplateService = taskTemplateService;
    }

    @ApiOperation("模板列表（分页）")
    @GetMapping
    public ApiResult<PageResult<TaskTemplate>> list(@Valid PageDTO dto) {
        java.util.List<TaskTemplate> all = taskTemplateService.listAll();
        int start = (int) ((dto.getPageNum() - 1) * dto.getPageSize());
        java.util.List<TaskTemplate> page = all.stream().skip(start).limit(dto.getPageSize()).collect(java.util.stream.Collectors.toList());
        return ApiResult.ok(new PageResult<>(dto.getPageNum(), dto.getPageSize(), (long) all.size(), page));
    }

    @ApiOperation("模板详情")
    @GetMapping("/{id}")
    public ApiResult<TaskTemplate> detail(@PathVariable Long id) {
        return ApiResult.ok(taskTemplateService.detail(id));
    }

    @OperateLog(module = "任务模板", action = "CREATE", targetType = "TASK_TEMPLATE")
    @ApiOperation("新增模板")
    @PostMapping
    public ApiResult<TaskTemplate> create(@Valid @RequestBody TaskTemplate entity) {
        return ApiResult.ok(taskTemplateService.create(entity));
    }

    @OperateLog(module = "任务模板", action = "UPDATE", targetType = "TASK_TEMPLATE", targetIdExpr = "#id")
    @ApiOperation("更新模板")
    @PutMapping("/{id}")
    public ApiResult<TaskTemplate> update(@PathVariable Long id, @Valid @RequestBody TaskTemplate entity) {
        return ApiResult.ok(taskTemplateService.update(id, entity));
    }

    @OperateLog(module = "任务模板", action = "DELETE", targetType = "TASK_TEMPLATE", targetIdExpr = "#id")
    @ApiOperation("删除模板")
    @DeleteMapping("/{id}")
    public ApiResult<Void> delete(@PathVariable Long id) {
        taskTemplateService.delete(id);
        return ApiResult.ok();
    }

    @OperateLog(module = "任务模板", action = "UPDATE", targetType = "TASK_TEMPLATE", targetIdExpr = "#id")
    @ApiOperation("启停模板")
    @PutMapping("/{id}/toggle")
    public ApiResult<Void> toggle(@PathVariable Long id) {
        taskTemplateService.toggleStatus(id);
        return ApiResult.ok();
    }

    @ApiOperation("按动作ID查关联模板")
    @GetMapping("/by-action/{actionId}")
    public ApiResult<List<TaskTemplate>> listByActionId(@PathVariable Long actionId) {
        return ApiResult.ok(taskTemplateService.listByActionId(actionId));
    }
}
