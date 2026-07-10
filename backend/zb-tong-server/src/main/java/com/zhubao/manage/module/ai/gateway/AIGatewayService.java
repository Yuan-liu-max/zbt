package com.zhubao.manage.module.ai.gateway;

import java.util.Map;

/**
 * AI服务网关接口 —— 策略模式适配不同大模型
 */
public interface AIGatewayService {

    /**
     * 调用大模型
     * @param systemPrompt 系统提示词
     * @param userPrompt   用户提示词（含数据）
     * @return {outputText, tokenUsage}
     */
    Map<String, String> chat(String systemPrompt, String userPrompt);

    /** 网关名称 */
    String getName();
}
