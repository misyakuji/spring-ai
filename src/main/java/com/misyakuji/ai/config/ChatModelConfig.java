package com.misyakuji.ai.config;

import org.springframework.ai.deepseek.DeepSeekChatModel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class ChatModelConfig {

    @Bean
    @Primary
    public org.springframework.ai.chat.model.ChatModel deepSeekPrimaryChatModel(DeepSeekChatModel deepSeekChatModel) {
        return deepSeekChatModel;
    }
}
