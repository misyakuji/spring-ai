package com.misyakuji.ai.controller;

import com.misyakuji.ai.dto.KnowledgeBaseCreateRequest;
import com.misyakuji.ai.service.KnowledgeImportService;
import com.misyakuji.ai.service.KnowledgeSearchService;
import com.misyakuji.ai.repository.KnowledgeBaseRepository;
import com.misyakuji.ai.repository.KnowledgeDocumentRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webflux.test.autoconfigure.WebFluxTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@WebFluxTest(KnowledgeBaseController.class)
class KnowledgeBaseControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockitoBean
    private KnowledgeBaseRepository kbRepo;

    @MockitoBean
    private KnowledgeDocumentRepository docRepo;

    @MockitoBean
    private KnowledgeImportService importService;

    @MockitoBean
    private KnowledgeSearchService searchService;

    @Test
    @DisplayName("POST /api/v1/knowledge/bases - 创建知识库成功")
    void createKnowledgeBase_success() {
        KnowledgeBaseCreateRequest request = new KnowledgeBaseCreateRequest();
        request.setName("测试知识库");
        request.setDescription("测试描述");

        when(kbRepo.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        webTestClient.post()
                .uri("/api/v1/knowledge/bases")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.name").isEqualTo("测试知识库")
                .jsonPath("$.description").isEqualTo("测试描述");
    }

    @Test
    @DisplayName("POST /api/v1/knowledge/bases - 缺少name返回400")
    void createKnowledgeBase_missingName_shouldReturn400() {
        webTestClient.post()
                .uri("/api/v1/knowledge/bases")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
                        {"description":"缺少name"}
                        """)
                .exchange()
                .expectStatus().is4xxClientError();
    }

    @Test
    @DisplayName("GET /api/v1/knowledge/bases - 获取知识库列表")
    void listKnowledgeBases_success() {
        when(kbRepo.findAll()).thenReturn(List.of());

        webTestClient.get()
                .uri("/api/v1/knowledge/bases")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$").isArray();
    }

    @Test
    @DisplayName("DELETE /api/v1/knowledge/bases/{id} - 删除知识库成功")
    void deleteKnowledgeBase_success() {
        doNothing().when(kbRepo).deleteById("kb-123");

        webTestClient.delete()
                .uri("/api/v1/knowledge/bases/kb-123")
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    @DisplayName("DELETE /api/v1/knowledge/documents/{id} - 删除文档成功")
    void deleteDocument_success() {
        doNothing().when(docRepo).deleteById("doc-123");

        webTestClient.delete()
                .uri("/api/v1/knowledge/documents/doc-123")
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    @DisplayName("POST /api/v1/knowledge/search - 向量检索成功")
    void searchVectors_success() {
        when(searchService.search(anyString(), anyString(), anyInt(), anyDouble()))
                .thenReturn(List.of(Map.of("content", "测试片段", "score", 0.95)));

        webTestClient.post()
                .uri("/api/v1/knowledge/search")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
                        {"query":"测试查询","knowledgeBaseId":"kb-123","topK":5}
                        """)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(Object.class)
                .hasSize(1);
    }

    @Test
    @DisplayName("POST /api/v1/knowledge/search - 缺少必填字段返回400")
    void searchVectors_missingFields_shouldReturn400() {
        webTestClient.post()
                .uri("/api/v1/knowledge/search")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
                        {"query":"测试"}
                        """)
                .exchange()
                .expectStatus().is4xxClientError();
    }
}
