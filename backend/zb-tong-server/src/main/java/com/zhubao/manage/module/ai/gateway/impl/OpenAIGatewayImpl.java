package com.zhubao.manage.module.ai.gateway.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zhubao.manage.common.exception.BusinessException;
import com.zhubao.manage.common.exception.ErrorCode;
import com.zhubao.manage.module.ai.gateway.AIGatewayService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;
import java.util.*;

/**
 * OpenAI 兼容网关 —— 同时支持 OpenAI / DeepSeek / 各类兼容中转
 * 通过 ai.openai.base-url 指定 API 地址（OpenAI 兼容协议均可）
 */
@Component
@ConditionalOnProperty(name = "ai.gateway", havingValue = "openai", matchIfMissing = true)
public class OpenAIGatewayImpl implements AIGatewayService {

    private static final Logger log = LoggerFactory.getLogger(OpenAIGatewayImpl.class);

    @Value("${ai.openai.api-key}")
    private String apiKey;

    @Value("${ai.openai.model:gpt-4}")
    private String model;

    @Value("${ai.openai.timeout:120}")
    private int timeout;

    @Value("${ai.openai.base-url:https://api.openai.com/v1}")
    private String baseUrl;

    private RestTemplate restTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @PostConstruct
    public void init() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(timeout * 1000);
        factory.setReadTimeout(timeout * 1000);
        this.restTemplate = new RestTemplate(factory);
    }

    @Override
    public Map<String, String> chat(String systemPrompt, String userPrompt) {
        if (apiKey == null || apiKey.trim().isEmpty() || "sk-your-key".equals(apiKey.trim())) {
            throw new BusinessException(ErrorCode.AI_SERVICE_ERROR,
                    "AI 服务未配置有效的 API Key，请联系管理员配置后使用");
        }

        String url = (baseUrl == null || baseUrl.trim().isEmpty())
                ? "https://api.openai.com/v1" : baseUrl.trim();
        if (!url.endsWith("/")) url += "/";

        List<Map<String, String>> messages = new ArrayList<>();
        messages.add(msg("system", systemPrompt));
        messages.add(msg("user", userPrompt));

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("model", model);
        body.put("messages", messages);
        body.put("temperature", 0.7);
        body.put("max_tokens", 4000);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey.trim());
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<String> resp = restTemplate.postForEntity(url + "chat/completions", entity, String.class);
            JsonNode root = objectMapper.readTree(resp.getBody());
            JsonNode messageNode = root.path("choices").path(0).path("message");
            String content = messageNode.path("content").asText("");
            long tokens = root.path("usage").path("total_tokens").asLong(0);

            Map<String, String> result = new LinkedHashMap<>();
            result.put("outputText", content);
            result.put("tokenUsage", String.valueOf(tokens));
            log.info("AI 调用成功: baseUrl={}, model={}, tokens={}", url, model, tokens);
            return result;
        } catch (HttpStatusCodeException e) {
            log.error("AI 调用失败: status={}, resp={}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new BusinessException(ErrorCode.AI_SERVICE_ERROR,
                    "AI 服务调用失败(" + e.getStatusCode().value() + ")，请检查 API Key、模型名称与网络");
        } catch (Exception e) {
            log.error("AI 调用失败", e);
            throw new BusinessException(ErrorCode.AI_SERVICE_ERROR, "AI 服务暂时不可用，请稍后重试");
        }
    }

    private Map<String, String> msg(String role, String content) {
        Map<String, String> m = new LinkedHashMap<>();
        m.put("role", role);
        m.put("content", content);
        return m;
    }

    @Override
    public String getName() { return "OPENAI"; }
}
