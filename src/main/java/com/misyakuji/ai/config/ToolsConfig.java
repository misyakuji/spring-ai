package com.misyakuji.ai.config;

import com.misyakuji.ai.tools.BusinessTools;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ToolsConfig {

    @Bean
    public ToolCallbackProvider toolCallbackProvider(BusinessTools businessTools) {
        return MethodToolCallbackProvider.builder()
                .toolObjects(businessTools)
                .build();
    }
}
