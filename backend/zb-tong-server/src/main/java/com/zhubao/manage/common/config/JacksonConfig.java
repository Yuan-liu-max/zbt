package com.zhubao.manage.common.config;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;

/**
 * Jackson 全局配置 —— 枚举大写序列化
 */
@Configuration
public class JacksonConfig {

    @Bean
    public Jackson2ObjectMapperBuilderCustomizer enumUppercaseCustomizer() {
        return builder -> {
            builder.serializerByType(Enum.class, new UppercaseEnumSerializer());
        };
    }

    /**
     * 枚举序列化器 —— 输出 name().toUpperCase() 即枚举常量名本身（已是大写）
     */
    static class UppercaseEnumSerializer extends StdSerializer<Enum<?>> {

        protected UppercaseEnumSerializer() {
            super(Enum.class, false);
        }

        @Override
        public void serialize(Enum<?> value, JsonGenerator gen, SerializerProvider provider) throws IOException {
            gen.writeString(value.name());  // Java 枚举常量名天然全大写
        }
    }
}
