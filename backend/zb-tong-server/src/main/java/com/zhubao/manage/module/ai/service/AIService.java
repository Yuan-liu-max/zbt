package com.zhubao.manage.module.ai.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zhubao.manage.common.exception.BusinessException;
import com.zhubao.manage.common.exception.ErrorCode;
import com.zhubao.manage.module.ai.assembler.DataAssembler;
import com.zhubao.manage.module.ai.entity.AiChatHistory;
import com.zhubao.manage.module.ai.entity.AIResult;
import com.zhubao.manage.module.ai.entity.PromptTemplate;
import com.zhubao.manage.module.ai.gateway.AIGatewayService;
import com.zhubao.manage.module.ai.mapper.AiChatHistoryMapper;
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
    private final AsyncAIService asyncAIService;
    private final AiChatHistoryMapper aiChatHistoryMapper;

    public AIService(AIGatewayService gs, DataAssembler da, ResultParser rp,
                     PromptTemplateService pts, AIResultMapper arm,
                     TaskInstanceMapper tim, AIScoreService ass, AsyncAIService aas,
                     AiChatHistoryMapper aichm) {
        this.gatewayService = gs; this.dataAssembler = da; this.resultParser = rp;
        this.promptTemplateService = pts; this.aiResultMapper = arm;
        this.taskInstanceMapper = tim; this.aiScoreService = ass;
        this.asyncAIService = aas; this.aiChatHistoryMapper = aichm;
    }

    // ============ AI建议（同步返回，异步执行） ============

    public Map<String, Object> getEmployeeAdvice(Long userId) { return analyze("EMPLOYEE", userId); }
    public Map<String, Object> getProductAdvice(Long storeId) { return analyze("PRODUCT", storeId); }
    public Map<String, Object> getSceneAdvice(Long storeId) { return analyze("SCENE", storeId); }

    /** 门店综合建议（货+场聚合分析，输出 STORE 类型结果） */
    public Map<String, Object> getStoreAdvice(Long storeId) {
        return analyze("STORE", storeId);
    }

    /** 通用分析（分析提示词固定用内置默认值，不依赖提示词模板表） */
    public Map<String, Object> analyze(String businessType, Long relatedId) {
        String inputSnapshot = dataAssembler.assemble(businessType, relatedId);
        String systemPrompt = getDefaultPrompt(businessType);
        asyncAIService.triggerAsyncAI(businessType, relatedId, null, systemPrompt, inputSnapshot);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("status", "processing");
        result.put("businessType", businessType);
        result.put("relatedId", relatedId);
        result.put("message", "AI分析已触发，结果将写入 ai_result 表");
        return result;
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

    /** AI智能问答（同步调用，失败返回友好错误） */
    public Map<String, Object> chat(String question) {
        if (question == null || question.trim().isEmpty()) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "问题不能为空");
        }
        String systemPrompt = "你是一位珠宝零售门店管理助手（掌宝通）。请用简洁、专业的中文回答门店经营管理、销售、库存、任务、人事等问题。无法确定的信息请如实说明，不要编造。";
        try {
            Map<String, String> response = gatewayService.chat(systemPrompt, question.trim());
            String reply = response.get("outputText");
            saveChatHistory(question.trim(), reply);
            Map<String, Object> result = new LinkedHashMap<>();
            result.put("reply", reply);
            result.put("modelName", gatewayService.getName());
            result.put("tokenUsage", response.get("tokenUsage"));
            return result;
        } catch (BusinessException be) {
            throw be;
        } catch (Exception e) {
            log.error("AI问答失败", e);
            throw new BusinessException(ErrorCode.AI_SERVICE_ERROR, "AI服务暂时不可用，请稍后重试");
        }
    }

    /** 保存问答历史 */
    private void saveChatHistory(String question, String answer) {
        AiChatHistory h = new AiChatHistory();
        h.setQuestion(question);
        h.setAnswer(answer);
        h.setModelName(gatewayService.getName());
        aiChatHistoryMapper.insert(h);
    }

    /** 查询问答历史（最近50条） */
    public List<AiChatHistory> getChatHistory() {
        return aiChatHistoryMapper.selectList(
                new LambdaQueryWrapper<AiChatHistory>()
                        .orderByDesc(AiChatHistory::getCreatedAt)
                        .last("LIMIT 50"));
    }

    /** 清空问答历史 */
    public void clearChatHistory() {
        aiChatHistoryMapper.delete(new LambdaQueryWrapper<>());
    }

    /** 基于提示词模板 + 用户内容生成文档 */
    public Map<String, Object> generateDoc(Long templateId, String content) {
        PromptTemplate template = templateId != null ? promptTemplateService.detail(templateId) : null;
        StringBuilder userPrompt = new StringBuilder();
        if (template != null && template.getPromptContent() != null && !template.getPromptContent().trim().isEmpty()) {
            userPrompt.append(template.getPromptContent().trim()).append("\n\n");
        } else {
            userPrompt.append("请根据以下内容生成一份专业、结构清晰、可直接使用的文档：\n\n");
        }
        userPrompt.append("【用户提供的内容】\n").append(content == null ? "" : content);
        String systemPrompt = "你是一位专业的中文文档撰写助手。请严格依据模板要求与用户提供的内容，生成条理清晰、格式规范、可直接使用的文档正文。";
        Map<String, String> response = gatewayService.chat(systemPrompt, userPrompt.toString());
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("content", response.get("outputText"));
        result.put("modelName", gatewayService.getName());
        result.put("tokenUsage", response.get("tokenUsage"));
        return result;
    }

    private String extractJsonOutput(String outputText) {
        Map<String, Object> parsed = resultParser.parseToMap(outputText);
        if (parsed.isEmpty()) return outputText;
        try { return new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(parsed); }
        catch (Exception e) { return outputText; }
    }

    private String getDefaultPrompt(String businessType) {
        String schema;
        switch (businessType) {
            case "EMPLOYEE":
                schema = "{\"员工优势\":[\"\"],\"待提升领域\":[\"\"],\"可操作建议\":[\"\"]}";
                return "你是一位珠宝零售行业的人力资源专家。请根据以下员工数据，分析该员工的表现。只输出JSON，不要输出任何其它文字或markdown代码块，格式严格如下：" + schema;
            case "PRODUCT":
                schema = "{\"商品结构分析\":\"\",\"动销分析\":\"\",\"运营建议\":[\"\"]}";
                return "你是一位珠宝零售行业的商品运营专家。请根据以下商品数据，分析商品结构与动销情况。只输出JSON，不要输出任何其它文字或markdown代码块，格式严格如下：" + schema;
            case "SCENE":
                schema = "{\"运营质量分析\":\"\",\"存在问题\":[\"\"],\"改进建议\":[\"\"]}";
                return "你是一位珠宝零售门店运营专家。请根据以下巡检数据，分析门店运营质量。只输出JSON，不要输出任何其它文字或markdown代码块，格式严格如下：" + schema;
            case "STORE":
                schema = "{\"门店综合诊断\":\"\",\"核心问题\":[\"\"],\"改进建议\":[\"\"]}";
                return "你是一位珠宝零售门店综合诊断专家。请根据以下门店的货品与场景巡检数据，给出门店综合诊断。只输出JSON，不要输出任何其它文字或markdown代码块，格式严格如下：" + schema;
            default:
                schema = "{\"分析\":\"\",\"建议\":[\"\"]}";
                return "请分析以下数据并给出建议。只输出JSON，不要输出任何其它文字或markdown代码块，格式严格如下：" + schema;
        }
    }
}
