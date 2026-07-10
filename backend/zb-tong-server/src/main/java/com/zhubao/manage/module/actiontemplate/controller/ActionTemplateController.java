package com.zhubao.manage.module.actiontemplate.controller;

import com.zhubao.manage.common.dto.ApiResult;
import com.zhubao.manage.module.actiontemplate.dto.DispatchToStoresDTO;
import com.zhubao.manage.module.actiontemplate.entity.ActionTemplate;
import com.zhubao.manage.module.actiontemplate.service.ActionTemplateService;
import com.zhubao.manage.module.task.entity.TaskInstance;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@Api(tags = "动作库管理")
@RestController
@RequestMapping("/action-templates")
public class ActionTemplateController {

    private final ActionTemplateService actionTemplateService;

    public ActionTemplateController(ActionTemplateService actionTemplateService) {
        this.actionTemplateService = actionTemplateService;
    }

    @ApiOperation("动作库列表")
    @GetMapping
    public ApiResult<List<ActionTemplate>> list() {
        return ApiResult.ok(actionTemplateService.listAll());
    }

    @ApiOperation("动作详情")
    @GetMapping("/{id}")
    public ApiResult<ActionTemplate> detail(@PathVariable Long id) {
        return ApiResult.ok(actionTemplateService.detail(id));
    }

    @ApiOperation("新增动作")
    @PostMapping
    public ApiResult<ActionTemplate> create(@RequestBody ActionTemplate entity) {
        return ApiResult.ok(actionTemplateService.create(entity));
    }

    @ApiOperation("更新动作")
    @PutMapping("/{id}")
    public ApiResult<ActionTemplate> update(@PathVariable Long id, @RequestBody ActionTemplate entity) {
        return ApiResult.ok(actionTemplateService.update(id, entity));
    }

    @ApiOperation("删除动作")
    @DeleteMapping("/{id}")
    public ApiResult<Void> delete(@PathVariable Long id) {
        actionTemplateService.delete(id);
        return ApiResult.ok();
    }

    @ApiOperation("启停动作")
    @PutMapping("/{id}/toggle")
    public ApiResult<Void> toggle(@PathVariable Long id) {
        actionTemplateService.toggleStatus(id);
        return ApiResult.ok();
    }

    @ApiOperation("一键下发到门店")
    @PostMapping("/dispatch")
    public ApiResult<List<TaskInstance>> dispatch(@Valid @RequestBody DispatchToStoresDTO dto) {
        return ApiResult.ok(actionTemplateService.dispatchToStores(dto.getActionId(), dto.getStoreIds()));
    }
}
