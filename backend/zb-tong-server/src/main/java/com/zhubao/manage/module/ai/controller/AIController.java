package com.zhubao.manage.module.ai.controller;

import com.zhubao.manage.common.dto.ApiResult;
import com.zhubao.manage.module.ai.entity.AiChatHistory;
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
@org.springframework.security.access.prepost.PreAuthorize("isAuthenticated()")
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

    @ApiOperation("AI建议（通用入口，兼容前端 GET /ai/advice/{type}/{id}）")
    @GetMapping("/advice/{type}/{id}")
    public ApiResult<Map<String, Object>> getAdvice(@PathVariable String type, @PathVariable Long id) {
        switch (type) {
            case "employee": return ApiResult.ok(aiService.getEmployeeAdvice(id));
            case "product":  return ApiResult.ok(aiService.getProductAdvice(id));
            case "scene":    return ApiResult.ok(aiService.getSceneAdvice(id));
            case "store":    return ApiResult.ok(aiService.getStoreAdvice(id));
            default:         return ApiResult.fail("未知 type: " + type);
        }
    }

    // ===== AI智能问答 =====

    @ApiOperation("AI智能问答")
    @PostMapping("/chat")
    public ApiResult<Map<String, Object>> chat(@RequestBody Map<String, String> body) {
        return ApiResult.ok(aiService.chat(body.get("question")));
    }

    // ===== AI工具列表 =====

    @ApiOperation("AI工具列表")
    @GetMapping("/tools")
    public ApiResult<java.util.List<java.util.Map<String, Object>>> tools() {
        java.util.List<java.util.Map<String, Object>> list = new java.util.ArrayList<>();
        addTool(list, "employee", "员工建议", "AI员工画像分析与辅导建议");
        addTool(list, "product", "货品建议", "AI货品运营分析与推荐");
        addTool(list, "scene", "场景建议", "AI场景问题诊断与优化");
        addTool(list, "store", "门店综合建议", "AI门店综合诊断报告");
        addTool(list, "score", "任务评分", "AI任务执行质量评分");
        addTool(list, "dataAnalysis", "数据分析", "AI经营数据分析与洞察");
        addTool(list, "doc", "文档生成", "基于提示词模板生成经营文档");
        return ApiResult.ok(list);
    }
    private void addTool(java.util.List<java.util.Map<String, Object>> list, String id, String name, String desc) {
        java.util.Map<String, Object> m = new java.util.LinkedHashMap<>();
        m.put("id", id); m.put("name", name); m.put("description", desc);
        list.add(m);
    }

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

    @ApiOperation("AI问答历史") @GetMapping("/chat-history")
    public ApiResult<List<AiChatHistory>> chatHistory() { return ApiResult.ok(aiService.getChatHistory()); }

    @ApiOperation("清空AI问答历史") @DeleteMapping("/chat-history")
    public ApiResult<Void> clearChatHistory() { aiService.clearChatHistory(); return ApiResult.ok(); }

    // ===== 文档生成 =====

    @ApiOperation("文档生成（提示词模板 + 内容 → AI生成）") @PostMapping("/doc/generate")
    public ApiResult<Map<String, Object>> generateDoc(@RequestBody Map<String, Object> body) {
        Long templateId = body.get("templateId") == null ? null : Long.valueOf(body.get("templateId").toString());
        String content = body.get("content") == null ? "" : body.get("content").toString();
        return ApiResult.ok(aiService.generateDoc(templateId, content));
    }

    // ===== 提示词模板管理 =====

    @ApiOperation("提示词模板列表") @GetMapping("/prompts")
    public ApiResult<List<PromptTemplate>> listPrompts() { return ApiResult.ok(promptTemplateService.listAll()); }

    @ApiOperation("提示词模板列表（兼容前端 /ai/prompt-templates）") @GetMapping("/prompt-templates")
    public ApiResult<List<PromptTemplate>> listPromptTemplates() { return ApiResult.ok(promptTemplateService.listAll()); }

    @ApiOperation("新增提示词模板") @PostMapping("/prompts")
    public ApiResult<PromptTemplate> createPrompt(@RequestBody PromptTemplate pt) { return ApiResult.ok(promptTemplateService.create(pt)); }

    @ApiOperation("新增提示词模板（兼容）") @PostMapping("/prompt-templates")
    public ApiResult<PromptTemplate> createPromptTemplate(@RequestBody PromptTemplate pt) { return ApiResult.ok(promptTemplateService.create(pt)); }

    @ApiOperation("更新提示词模板") @PutMapping("/prompts/{id}")
    public ApiResult<PromptTemplate> updatePrompt(@PathVariable Long id, @RequestBody PromptTemplate pt) { return ApiResult.ok(promptTemplateService.update(id, pt)); }

    @ApiOperation("更新提示词模板（兼容）") @PutMapping("/prompt-templates/{id}")
    public ApiResult<PromptTemplate> updatePromptTemplate(@PathVariable Long id, @RequestBody PromptTemplate pt) { return ApiResult.ok(promptTemplateService.update(id, pt)); }

    @ApiOperation("删除提示词模板") @DeleteMapping("/prompts/{id}")
    public ApiResult<Void> deletePrompt(@PathVariable Long id) { promptTemplateService.delete(id); return ApiResult.ok(); }

    @ApiOperation("删除提示词模板（兼容）") @DeleteMapping("/prompt-templates/{id}")
    public ApiResult<Void> deletePromptTemplate(@PathVariable Long id) { promptTemplateService.delete(id); return ApiResult.ok(); }
}
