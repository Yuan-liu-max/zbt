package com.zhubao.manage.module.ai.service;

import com.zhubao.manage.module.ai.entity.AIResult;
import com.zhubao.manage.module.ai.gateway.AIGatewayService;
import com.zhubao.manage.module.ai.mapper.AIResultMapper;
import com.zhubao.manage.module.ai.parser.ResultParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

/**
 * 异步AI调用服务（P2-7: 从 AIService 提取，确保 @Async 通过 Spring AOP 代理生效）
 */
@Service
public class AsyncAIService {

    private static final Logger log = LoggerFactory.getLogger(AsyncAIService.class);

    private final AIGatewayService gatewayService;
    private final AIResultMapper aiResultMapper;
    private final ResultParser resultParser;

    public AsyncAIService(AIGatewayService gs, AIResultMapper arm, ResultParser rp) {
        this.gatewayService = gs; this.aiResultMapper = arm; this.resultParser = rp;
    }

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
            AIResult fail = new AIResult();
            fail.setBusinessType(businessType); fail.setRelatedId(relatedId);
            fail.setPromptTemplateId(templateId); fail.setInputSnapshot(inputSnapshot);
            fail.setOutputText(e.getMessage()); fail.setStatus("FAILED");
            fail.setModelName(gatewayService.getName());
            aiResultMapper.insert(fail);
        }
    }

    private String extractJsonOutput(String outputText) {
        Map<String, Object> parsed = resultParser.parseToMap(outputText);
        if (parsed.isEmpty()) return outputText;
        try { return new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(parsed); }
        catch (Exception e) { return outputText; }
    }
}
