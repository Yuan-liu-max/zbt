package com.zhubao.manage.module.task.controller;

import com.zhubao.manage.common.dto.ApiResult;
import com.zhubao.manage.module.task.entity.TaskTemplate;
import com.zhubao.manage.module.task.service.TaskTemplateService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Api(tags = "任务模板管理")
@RestController
@RequestMapping("/task-templates")
public class TaskTemplateController {

    private final TaskTemplateService taskTemplateService;

    public TaskTemplateController(TaskTemplateService taskTemplateService) {
        this.taskTemplateService = taskTemplateService;
    }

    @ApiOperation("模板列表")
    @GetMapping
    public ApiResult<List<TaskTemplate>> list() {
        return ApiResult.ok(taskTemplateService.listAll());
    }

    @ApiOperation("模板详情")
    @GetMapping("/{id}")
    public ApiResult<TaskTemplate> detail(@PathVariable Long id) {
        return ApiResult.ok(taskTemplateService.detail(id));
    }

    @ApiOperation("新增模板")
    @PostMapping
    public ApiResult<TaskTemplate> create(@RequestBody TaskTemplate entity) {
        return ApiResult.ok(taskTemplateService.create(entity));
    }

    @ApiOperation("更新模板")
    @PutMapping("/{id}")
    public ApiResult<TaskTemplate> update(@PathVariable Long id, @RequestBody TaskTemplate entity) {
        return ApiResult.ok(taskTemplateService.update(id, entity));
    }

    @ApiOperation("删除模板")
    @DeleteMapping("/{id}")
    public ApiResult<Void> delete(@PathVariable Long id) {
        taskTemplateService.delete(id);
        return ApiResult.ok();
    }

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
