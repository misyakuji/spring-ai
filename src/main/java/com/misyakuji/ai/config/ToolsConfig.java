package com.misyakuji.ai.config;

import com.misyakuji.ai.tools.BusinessTools;
import com.misyakuji.ai.tools.KnowledgeSearchTool;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.context.annotation.Bean;

@org.springframework.context.annotation.Configuration
public class ToolsConfig {

    @Bean
    public ToolCallbackProvider toolCallbackProvider(BusinessTools businessTools, KnowledgeSearchTool knowledgeSearchTool) {
        return MethodToolCallbackProvider.builder()
                .toolObjects(businessTools, knowledgeSearchTool)
                .build();
    }
}
