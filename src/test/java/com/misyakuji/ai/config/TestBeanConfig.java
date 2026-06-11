package com.misyakuji.ai.config;

import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

import static org.mockito.Mockito.mock;

@TestConfiguration
public class TestBeanConfig {

    @Bean
    @Primary
    public VectorStore vectorStore() {
        return mock(VectorStore.class);
    }
}
