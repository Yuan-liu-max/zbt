package com.zhubao.manage.module.ai.controller;

import com.zhubao.manage.common.dto.ApiResult;
import com.zhubao.manage.module.ai.entity.AIResult;
import com.zhubao.manage.module.ai.entity.PromptTemplate;
import com.zhubao.manage.module.ai.service.AIScoreService;
import com.zhubao.manage.module.ai.service.AIService;
import com.zhubao.manage.module.ai.service.PromptTemplateService;
import com.zhubao.manage.module.task.entity.TaskInstance;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Api(tags = "AI智能辅助")
@RestController
@RequestMapping("/ai")
public class AIController {

    private final AIService aiService;
    private final AIScoreService aiScoreService;
    private final PromptTemplateService promptTemplateService;

    public AIController(AIService ais, AIScoreService ass, PromptTemplateService pts) {
        this.aiService = ais; this.aiScoreService = ass; this.promptTemplateService = pts;
    }

    // ===== AI建议 =====

    @ApiOperation("AI员工建议") @PostMapping("/employee/{userId}/advice")
    public ApiResult<Map<String, Object>> employeeAdvice(@PathVariable Long userId) { return ApiResult.ok(aiService.getEmployeeAdvice(userId)); }

    @ApiOperation("AI货品建议") @PostMapping("/product/{storeId}/advice")
    public ApiResult<Map<String, Object>> productAdvice(@PathVariable Long storeId) { return ApiResult.ok(aiService.getProductAdvice(storeId)); }

    @ApiOperation("AI场景建议") @PostMapping("/scene/{storeId}/advice")
    public ApiResult<Map<String, Object>> sceneAdvice(@PathVariable Long storeId) { return ApiResult.ok(aiService.getSceneAdvice(storeId)); }

    @ApiOperation("AI门店综合建议") @PostMapping("/store/{storeId}/advice")
    public ApiResult<Map<String, Object>> storeAdvice(@PathVariable Long storeId) { return ApiResult.ok(aiService.getStoreAdvice(storeId)); }

    // ===== AI评分 =====

    @ApiOperation("AI任务执行质量评分") @PostMapping("/score/task/{taskId}")
    public ApiResult<Map<String, Object>> scoreTask(@PathVariable Long taskId) { return ApiResult.ok(aiScoreService.scoreTask(taskId)); }

    // ===== 一键转任务 =====

    @ApiOperation("AI建议一键转任务") @PostMapping("/result/{aiResultId}/convert")
    public ApiResult<TaskInstance> convertToTask(@PathVariable Long aiResultId, @RequestParam Long storeId, @RequestParam Long assigneeId) {
        return ApiResult.ok(aiService.convertToTask(aiResultId, storeId, assigneeId)); }

    // ===== 历史 =====

    @ApiOperation("AI结果历史") @GetMapping("/results")
    public ApiResult<List<AIResult>> results(@RequestParam(required = false) String businessType, @RequestParam(required = false) Long relatedId) {
        return ApiResult.ok(aiService.getHistory(businessType, relatedId)); }

    // ===== 提示词模板管理 =====

    @ApiOperation("提示词模板列表") @GetMapping("/prompts")
    public ApiResult<List<PromptTemplate>> listPrompts() { return ApiResult.ok(promptTemplateService.listAll()); }

    @ApiOperation("新增提示词模板") @PostMapping("/prompts")
    public ApiResult<PromptTemplate> createPrompt(@RequestBody PromptTemplate pt) { return ApiResult.ok(promptTemplateService.create(pt)); }

    @ApiOperation("更新提示词模板") @PutMapping("/prompts/{id}")
    public ApiResult<PromptTemplate> updatePrompt(@PathVariable Long id, @RequestBody PromptTemplate pt) { return ApiResult.ok(promptTemplateService.update(id, pt)); }

    @ApiOperation("删除提示词模板") @DeleteMapping("/prompts/{id}")
    public ApiResult<Void> deletePrompt(@PathVariable Long id) { promptTemplateService.delete(id); return ApiResult.ok(); }
}
