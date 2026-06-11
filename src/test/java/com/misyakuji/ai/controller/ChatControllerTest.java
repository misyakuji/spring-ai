package com.misyakuji.ai.controller;

import com.misyakuji.ai.dto.SessionCreateRequest;
import com.misyakuji.ai.dto.SessionResponse;
import com.misyakuji.ai.service.ChatService;
import com.misyakuji.ai.service.SessionService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webflux.test.autoconfigure.WebFluxTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@WebFluxTest(ChatController.class)
class ChatControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockitoBean
    private ChatService chatService;

    @MockitoBean
    private SessionService sessionService;

    @Test
    @DisplayName("POST /api/v1/chat/sessions - 创建会话成功")
    void createSession_success() {
        SessionCreateRequest request = new SessionCreateRequest();
        request.setUserId("test-user");
        request.setTitle("测试会话");

        SessionResponse response = new SessionResponse();
        response.setId("session-123");
        response.setUserId("test-user");
        response.setTitle("测试会话");
        response.setCreatedAt(LocalDateTime.now());

        when(sessionService.createSession(any(SessionCreateRequest.class))).thenReturn(response);

        webTestClient.post()
                .uri("/api/v1/chat/sessions")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.id").isEqualTo("session-123")
                .jsonPath("$.userId").isEqualTo("test-user")
                .jsonPath("$.title").isEqualTo("测试会话");
    }

    @Test
    @DisplayName("GET /api/v1/chat/sessions - 获取会话列表")
    void listSessions_success() {
        SessionResponse response = new SessionResponse();
        response.setId("session-123");
        response.setUserId("test-user");
        response.setTitle("测试会话");

        when(sessionService.listSessions("test-user")).thenReturn(List.of(response));

        webTestClient.get()
                .uri(uriBuilder -> uriBuilder.path("/api/v1/chat/sessions")
                        .queryParam("userId", "test-user").build())
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(SessionResponse.class)
                .hasSize(1);
    }

    @Test
    @DisplayName("DELETE /api/v1/chat/sessions/{id} - 删除会话成功")
    void deleteSession_success() {
        doNothing().when(sessionService).deleteSession("session-123");

        webTestClient.delete()
                .uri("/api/v1/chat/sessions/session-123")
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    @DisplayName("POST /api/v1/chat/completions - SSE流式聊天")
    void chatCompletions_sseStreaming() {
        when(chatService.streamChat(any())).thenReturn(Flux.just("你", "好", "！"));

        webTestClient.post()
                .uri("/api/v1/chat/completions")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
                        {"sessionId":"session-123","message":"你好"}
                        """)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().value("Content-Type", v -> v.contains("text/event-stream"));
    }

    @Test
    @DisplayName("POST /api/v1/chat/sessions - 缺少userId返回400")
    void createSession_missingUserId_shouldReturn400() {
        webTestClient.post()
                .uri("/api/v1/chat/sessions")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
                        {"title":"测试"}
                        """)
                .exchange()
                .expectStatus().is4xxClientError();
    }

    @Test
    @DisplayName("POST /api/v1/chat/completions - 缺少sessionId返回400")
    void chatCompletions_missingSessionId_shouldReturn400() {
        webTestClient.post()
                .uri("/api/v1/chat/completions")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
                        {"message":"你好"}
                        """)
                .exchange()
                .expectStatus().is4xxClientError();
    }
}
