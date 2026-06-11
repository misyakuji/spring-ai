package com.misyakuji.ai.controller;

import com.misyakuji.ai.dto.RagChatRequest;
import com.misyakuji.ai.service.RagChatService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webflux.test.autoconfigure.WebFluxTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@WebFluxTest(RagChatController.class)
class RagChatControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockitoBean
    private RagChatService ragChatService;

    @Test
    @DisplayName("POST /api/v1/rag/chat - SSE流式RAG对话")
    void ragChat_sseStreaming() {
        when(ragChatService.ragChat(any(RagChatRequest.class)))
                .thenReturn(Flux.just("根据", "知识库", "回答"));

        webTestClient.post()
                .uri("/api/v1/rag/chat")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
                        {"question":"测试问题","knowledgeBaseId":"kb-123","sessionId":"s1"}
                        """)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().value("Content-Type", v -> v.contains("text/event-stream"));
    }

    @Test
    @DisplayName("POST /api/v1/rag/chat - 缺少question返回400")
    void ragChat_missingQuestion_shouldReturn400() {
        webTestClient.post()
                .uri("/api/v1/rag/chat")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
                        {"knowledgeBaseId":"kb-123"}
                        """)
                .exchange()
                .expectStatus().is4xxClientError();
    }

    @Test
    @DisplayName("POST /api/v1/rag/chat - 缺少knowledgeBaseId返回400")
    void ragChat_missingKnowledgeBaseId_shouldReturn400() {
        webTestClient.post()
                .uri("/api/v1/rag/chat")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
                        {"question":"测试问题"}
                        """)
                .exchange()
                .expectStatus().is4xxClientError();
    }
}
