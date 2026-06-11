package com.misyakuji.ai.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webflux.test.autoconfigure.WebFluxTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.ai.tool.ToolCallbackProvider;

@WebFluxTest(ToolsController.class)
class ToolsControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockitoBean
    private ToolCallbackProvider toolCallbackProvider;

    @Test
    @DisplayName("GET /api/v1/tools - 返回工具列表")
    void listTools_success() {
        org.mockito.Mockito.when(toolCallbackProvider.getToolCallbacks())
                .thenReturn(new org.springframework.ai.tool.ToolCallback[0]);

        webTestClient.get()
                .uri("/api/v1/tools")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.count").isEqualTo(0)
                .jsonPath("$.tools").isArray();
    }
}
