package com.zhubao.manage.scheduler;

import com.zhubao.manage.module.ai.service.AIService;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * AI分析触发Job —— 每天7:00执行
 * 当 OPENAI_API_KEY 有效时真实调用 AIService，否则打印 WARN
 */
@Component
public class AIAnalysisJob {

    private static final Logger log = LoggerFactory.getLogger(AIAnalysisJob.class);

    private final AIService aiService;

    @Value("${ai.openai.api-key:sk-your-key}")
    private String apiKey;

    public AIAnalysisJob(AIService aiService) { this.aiService = aiService; }

    @XxlJob("aiAnalysisHandler")
    public void aiAnalysisHandler() {
        log.info("===== AI分析触发开始 =====");
        try {
            boolean real = isRealKey();
            if (real) {
                doRealAnalysis();
            } else {
                doStubAnalysis();
            }
        } catch (Exception e) {
            log.error("AI分析触发失败", e);
            XxlJobHelper.handleFail("AI分析触发失败: " + e.getMessage());
        }
    }

    private boolean isRealKey() {
        return apiKey != null && !apiKey.isEmpty() && !"sk-your-key".equals(apiKey);
    }

    private void doRealAnalysis() {
        int total = 0;
        try { aiService.analyze("EMPLOYEE", 0L); total++; log.info("[AI 1/3] 员工画像分析完成"); } catch (Exception e) { log.error("员工画像分析失败", e); }
        try { aiService.analyze("PRODUCT", 0L); total++; log.info("[AI 2/3] 货品运营分析完成"); } catch (Exception e) { log.error("货品运营分析失败", e); }
        try { aiService.analyze("SCENE", 0L); total++; log.info("[AI 3/3] 场景风险分析完成"); } catch (Exception e) { log.error("场景风险分析失败", e); }
        XxlJobHelper.handleSuccess("AI分析完成，已触发 " + total + " 项");
    }

    private void doStubAnalysis() {
        log.warn("[AI SKIP] OPENAI_API_KEY 未配置 (当前值: {}), 跳过AI分析", apiKey);
        log.info("[AI 1/3] 员工画像分析 - 跳过 (无API Key)");
        log.info("[AI 2/3] 货品运营分析 - 跳过 (无API Key)");
        log.info("[AI 3/3] 场景风险分析 - 跳过 (无API Key)");
        XxlJobHelper.handleSuccess("AI分析跳过 (无有效API Key)");
    }
}
