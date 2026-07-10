package com.zhubao.manage.module.ai.gateway.impl;

import com.theokanning.openai.completion.chat.ChatCompletionRequest;
import com.theokanning.openai.completion.chat.ChatMessage;
import com.theokanning.openai.service.OpenAiService;
import com.zhubao.manage.module.ai.gateway.AIGatewayService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.time.Duration;
import java.util.*;

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

    private OpenAiService openAiService;

    @PostConstruct
    public void init() {
        this.openAiService = new OpenAiService(apiKey, Duration.ofSeconds(timeout));
    }

    @Override
    public Map<String, String> chat(String systemPrompt, String userPrompt) {
        List<ChatMessage> messages = Arrays.asList(
                new ChatMessage("system", systemPrompt),
                new ChatMessage("user", userPrompt));

        ChatCompletionRequest request = ChatCompletionRequest.builder()
                .model(model)
                .messages(messages)
                .temperature(0.7)
                .maxTokens(2000)
                .build();

        com.theokanning.openai.completion.chat.ChatCompletionResult response = openAiService.createChatCompletion(request);
        String content = response.getChoices().get(0).getMessage().getContent();
        long tokens = response.getUsage().getTotalTokens();

        Map<String, String> result = new LinkedHashMap<>();
        result.put("outputText", content);
        result.put("tokenUsage", String.valueOf(tokens));
        log.info("OpenAI 调用成功: model={}, tokens={}", model, tokens);
        return result;
    }

    @Override
    public String getName() { return "OPENAI"; }
}
