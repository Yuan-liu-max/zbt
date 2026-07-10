package com.zhubao.manage.module.ai.parser;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * AI结果解析器 —— 解析大模型返回的JSON为Java对象
 */
@Component
public class ResultParser {

    private static final Logger log = LoggerFactory.getLogger(ResultParser.class);
    private static final ObjectMapper mapper = new ObjectMapper();

    /** 解析为通用 Map */
    @SuppressWarnings("unchecked")
    public Map<String, Object> parseToMap(String aiOutput) {
        try {
            if (aiOutput == null || aiOutput.trim().isEmpty()) return Collections.emptyMap();
            String json = extractJson(aiOutput);
            return mapper.readValue(json, Map.class);
        } catch (Exception e) {
            log.warn("AI结果解析失败，返回空Map: {}", e.getMessage());
            return Collections.emptyMap();
        }
    }

    /** 解析为指定类型 */
    public <T> T parseToObject(String aiOutput, Class<T> clazz) {
        try {
            String json = extractJson(aiOutput);
            return mapper.readValue(json, clazz);
        } catch (Exception e) {
            log.warn("AI结果解析失败: {}", e.getMessage());
            return null;
        }
    }

    /** 从AI输出中提取JSON块（处理markdown包裹的情况） */
    private String extractJson(String raw) {
        String s = raw.trim();
        // 去除 ```json ... ``` 包裹
        if (s.startsWith("```")) {
            int start = s.indexOf("\n");
            int end = s.lastIndexOf("```");
            if (start > 0 && end > start) s = s.substring(start, end).trim();
        }
        return s;
    }
}
