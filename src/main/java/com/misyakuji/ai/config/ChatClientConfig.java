package com.misyakuji.ai.config;

import com.misyakuji.ai.tools.BusinessTools;
import com.misyakuji.ai.tools.KnowledgeSearchTool;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ChatClientConfig {

    @Bean
    public ChatClient chatClient(ChatClient.Builder builder, ChatMemory chatMemory,
                                  BusinessTools businessTools, KnowledgeSearchTool knowledgeSearchTool) {
        return builder
                .defaultAdvisors(MessageChatMemoryAdvisor.builder(chatMemory).build())
                .defaultTools(businessTools, knowledgeSearchTool)
                .defaultSystem("你是一个专业的 AI 助手，请用中文回答。你可以使用工具来获取实时信息，如天气、时间、数学计算和知识库检索。")
                .build();
    }
}
