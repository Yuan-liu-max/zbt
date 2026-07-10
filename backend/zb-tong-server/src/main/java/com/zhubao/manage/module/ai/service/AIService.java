package com.zhubao.manage.module.ai.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zhubao.manage.module.ai.assembler.DataAssembler;
import com.zhubao.manage.module.ai.entity.AIResult;
import com.zhubao.manage.module.ai.entity.PromptTemplate;
import com.zhubao.manage.module.ai.gateway.AIGatewayService;
import com.zhubao.manage.module.ai.mapper.AIResultMapper;
import com.zhubao.manage.module.ai.parser.ResultParser;
import com.zhubao.manage.module.task.entity.TaskInstance;
import com.zhubao.manage.module.task.mapper.TaskInstanceMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class AIService {

    private static final Logger log = LoggerFactory.getLogger(AIService.class);

    private final AIGatewayService gatewayService;
    private final DataAssembler dataAssembler;
    private final ResultParser resultParser;
    private final PromptTemplateService promptTemplateService;
    private final AIResultMapper aiResultMapper;
    private final TaskInstanceMapper taskInstanceMapper;
    private final AIScoreService aiScoreService;

    public AIService(AIGatewayService gs, DataAssembler da, ResultParser rp,
                     PromptTemplateService pts, AIResultMapper arm,
                     TaskInstanceMapper tim, AIScoreService ass) {
        this.gatewayService = gs; this.dataAssembler = da; this.resultParser = rp;
        this.promptTemplateService = pts; this.aiResultMapper = arm;
        this.taskInstanceMapper = tim; this.aiScoreService = ass;
    }

    // ============ AI建议（同步返回，异步执行） ============

    public Map<String, Object> getEmployeeAdvice(Long userId) { return analyze("EMPLOYEE", userId); }
    public Map<String, Object> getProductAdvice(Long storeId) { return analyze("PRODUCT", storeId); }
    public Map<String, Object> getSceneAdvice(Long storeId) { return analyze("SCENE", storeId); }

    /** 聚合门店建议（人+货+场） */
    public Map<String, Object> getStoreAdvice(Long storeId) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("employee", analyze("EMPLOYEE", storeId));
        result.put("product", analyze("PRODUCT", storeId));
        result.put("scene", analyze("SCENE", storeId));
        return result;
    }

    /** 通用分析 */
    public Map<String, Object> analyze(String businessType, Long relatedId) {
        // 1. 组装数据
        String inputSnapshot = dataAssembler.assemble(businessType, relatedId);

        // 2. 查找提示词模板
        List<PromptTemplate> templates = promptTemplateService.listByBusinessType(businessType);
        String systemPrompt = templates.isEmpty() ? getDefaultPrompt(businessType) : templates.get(0).getPromptContent();
        Long templateId = templates.isEmpty() ? null : templates.get(0).getId();

        // 3. 异步调用大模型
        triggerAsyncAI(businessType, relatedId, templateId, systemPrompt, inputSnapshot);

        // 4. 立即返回（不阻塞）
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("status", "processing");
        result.put("businessType", businessType);
        result.put("relatedId", relatedId);
        result.put("message", "AI分析已触发，结果将写入 ai_result 表");
        return result;
    }

    /** @Async 异步执行AI调用 */
    @Async("aiExecutor")
    @Transactional
    public void triggerAsyncAI(String businessType, Long relatedId, Long templateId,
                                String systemPrompt, String inputSnapshot) {
        try {
            log.info("AI异步分析开始: type={}, id={}", businessType, relatedId);
            Map<String, String> response = gatewayService.chat(systemPrompt, inputSnapshot);

            AIResult aiResult = new AIResult();
            aiResult.setBusinessType(businessType);
            aiResult.setRelatedId(relatedId);
            aiResult.setPromptTemplateId(templateId);
            aiResult.setInputSnapshot(inputSnapshot);
            aiResult.setOutputText(response.get("outputText"));
            aiResult.setOutputJson(extractJsonOutput(response.get("outputText")));
            aiResult.setModelName(gatewayService.getName());
            aiResult.setTokenUsage(response.get("tokenUsage"));
            aiResult.setStatus("SUCCESS");
            aiResultMapper.insert(aiResult);

            log.info("AI异步分析完成: type={}, id={}, aiResultId={}", businessType, relatedId, aiResult.getId());
        } catch (Exception e) {
            log.error("AI异步分析失败: type={}, id={}", businessType, relatedId, e);
            // 失败记录
            AIResult fail = new AIResult();
            fail.setBusinessType(businessType); fail.setRelatedId(relatedId);
            fail.setPromptTemplateId(templateId); fail.setInputSnapshot(inputSnapshot);
            fail.setOutputText(e.getMessage()); fail.setStatus("FAILED");
            fail.setModelName(gatewayService.getName());
            aiResultMapper.insert(fail);
        }
    }

    // ============ AI建议一键转任务 ============

    @Transactional
    public TaskInstance convertToTask(Long aiResultId, Long storeId, Long assigneeId) {
        AIResult aiResult = aiResultMapper.selectById(aiResultId);
        if (aiResult == null || aiResult.getOutputJson() == null) {
            throw new RuntimeException("AI结果不存在或无结构化输出");
        }

        Map<String, Object> parsed = resultParser.parseToMap(aiResult.getOutputJson());
        String taskTitle = (String) parsed.getOrDefault("taskTitle", "AI建议任务-" + aiResultId);

        TaskInstance task = new TaskInstance();
        task.setTaskNo("AI" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")));
        task.setTaskTitle(taskTitle);
        task.setDimension("COMPREHENSIVE");
        task.setCategory("AI建议");
        task.setStoreId(storeId);
        task.setAssigneeId(assigneeId);
        task.setStartTime(LocalDateTime.now());
        task.setDueTime(LocalDateTime.now().plusDays(3));
        task.setStatus("PENDING");
        task.setPriority("MEDIUM");
        task.setSourceType("AI");
        task.setRelatedObjectType("AI_RESULT");
        task.setRelatedObjectId(aiResultId);
        taskInstanceMapper.insert(task);
        return task;
    }

    /** 获取AI历史结果 */
    public List<AIResult> getHistory(String businessType, Long relatedId) {
        return aiResultMapper.selectList(
                new LambdaQueryWrapper<AIResult>()
                        .eq(businessType != null, AIResult::getBusinessType, businessType)
                        .eq(relatedId != null, AIResult::getRelatedId, relatedId)
                        .orderByDesc(AIResult::getCreatedAt));
    }

    private String extractJsonOutput(String outputText) {
        Map<String, Object> parsed = resultParser.parseToMap(outputText);
        if (parsed.isEmpty()) return outputText;
        try { return new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(parsed); }
        catch (Exception e) { return outputText; }
    }

    private String getDefaultPrompt(String businessType) {
        switch (businessType) {
            case "EMPLOYEE": return "你是一位珠宝零售行业的人力资源专家。请根据以下员工数据，分析该员工的优势、待提升领域，并给出可操作的建议。以JSON格式输出。";
            case "PRODUCT": return "你是一位珠宝零售行业的商品运营专家。请根据以下商品数据，分析商品结构、动销情况，并给出运营建议。以JSON格式输出。";
            case "SCENE": return "你是一位珠宝零售门店运营专家。请根据以下巡检数据，分析门店运营质量，并给出改进建议。以JSON格式输出。";
            default: return "请分析以下数据并给出建议。以JSON格式输出。";
        }
    }
}
