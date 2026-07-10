package com.zhubao.manage.module.ai.gateway.impl;

import com.zhubao.manage.module.ai.gateway.AIGatewayService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * 文心一言网关（预留）
 *
 * TODO: 接入百度文心一言 API
 *   1. 获取 access_token (client_id + client_secret)
 *   2. 调用 /rpc/2.0/ai_custom/v1/wenxinworkshop/chat/completions
 */
//@Component
//@ConditionalOnProperty(name = "ai.gateway", havingValue = "wenxin")
public class WenXinGatewayImpl implements AIGatewayService {

    private static final Logger log = LoggerFactory.getLogger(WenXinGatewayImpl.class);

    @Override
    public Map<String, String> chat(String systemPrompt, String userPrompt) {
        log.warn("文心一言网关尚未接入");
        Map<String, String> result = new LinkedHashMap<>();
        result.put("outputText", "{}");
        result.put("tokenUsage", "0");
        return result;
    }

    @Override
    public String getName() { return "WENXIN"; }
}
