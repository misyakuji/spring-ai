package com.misyakuji.ai.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class DtoTest {

    @Test
    @DisplayName("ChatResponse - getter/setter正常")
    void chatResponse_getterSetter() {
        ChatResponse response = new ChatResponse();
        response.setSessionId("s1");
        response.setContent("你好");
        response.setDone(true);

        assertEquals("s1", response.getSessionId());
        assertEquals("你好", response.getContent());
        assertTrue(response.isDone());
    }

    @Test
    @DisplayName("ChatResponse - 构造函数")
    void chatResponse_constructor() {
        ChatResponse response = new ChatResponse("s1", "你好", false);

        assertEquals("s1", response.getSessionId());
        assertEquals("你好", response.getContent());
        assertFalse(response.isDone());
    }

    @Test
    @DisplayName("SessionResponse - getter/setter正常")
    void sessionResponse_getterSetter() {
        SessionResponse response = new SessionResponse();
        LocalDateTime now = LocalDateTime.now();
        response.setId("s1");
        response.setUserId("u1");
        response.setTitle("测试");
        response.setModel("deepseek-chat");
        response.setCreatedAt(now);
        response.setUpdatedAt(now);

        assertEquals("s1", response.getId());
        assertEquals("u1", response.getUserId());
        assertEquals("测试", response.getTitle());
        assertEquals("deepseek-chat", response.getModel());
        assertEquals(now, response.getCreatedAt());
        assertEquals(now, response.getUpdatedAt());
    }

    @Test
    @DisplayName("MessageResponse - getter/setter正常")
    void messageResponse_getterSetter() {
        MessageResponse response = new MessageResponse();
        response.setRole("user");
        response.setContent("你好");
        response.setTimestamp(1234567890L);

        assertEquals("user", response.getRole());
        assertEquals("你好", response.getContent());
        assertEquals(1234567890L, response.getTimestamp());
    }

    @Test
    @DisplayName("MessageResponse - 构造函数")
    void messageResponse_constructor() {
        MessageResponse response = new MessageResponse("assistant", "你好！", 999L);

        assertEquals("assistant", response.getRole());
        assertEquals("你好！", response.getContent());
        assertEquals(999L, response.getTimestamp());
    }

    @Test
    @DisplayName("KnowledgeBaseResponse - getter/setter正常")
    void knowledgeBaseResponse_getterSetter() {
        KnowledgeBaseResponse response = new KnowledgeBaseResponse();
        LocalDateTime now = LocalDateTime.now();
        response.setId("kb1");
        response.setName("测试");
        response.setDescription("描述");
        response.setEmbedModel("text-embedding-3-small");
        response.setCreatedAt(now);

        assertEquals("kb1", response.getId());
        assertEquals("测试", response.getName());
        assertEquals("描述", response.getDescription());
        assertEquals("text-embedding-3-small", response.getEmbedModel());
        assertEquals(now, response.getCreatedAt());
    }

    @Test
    @DisplayName("KnowledgeDocumentResponse - getter/setter正常")
    void knowledgeDocumentResponse_getterSetter() {
        KnowledgeDocumentResponse response = new KnowledgeDocumentResponse();
        response.setId("doc1");
        response.setKnowledgeBaseId("kb1");
        response.setFileName("test.pdf");
        response.setFileType("pdf");
        response.setFileSize(1024L);
        response.setChunkCount(10);
        response.setStatus("DONE");
        response.setErrorMsg(null);

        assertEquals("doc1", response.getId());
        assertEquals("kb1", response.getKnowledgeBaseId());
        assertEquals("test.pdf", response.getFileName());
        assertEquals("pdf", response.getFileType());
        assertEquals(1024L, response.getFileSize());
        assertEquals(10, response.getChunkCount());
        assertEquals("DONE", response.getStatus());
        assertNull(response.getErrorMsg());
    }

    @Test
    @DisplayName("ChatRequest - getter/setter正常")
    void chatRequest_getterSetter() {
        ChatRequest request = new ChatRequest();
        request.setSessionId("s1");
        request.setMessage("你好");
        request.setUserId("u1");

        assertEquals("s1", request.getSessionId());
        assertEquals("你好", request.getMessage());
        assertEquals("u1", request.getUserId());
    }

    @Test
    @DisplayName("SessionCreateRequest - getter/setter正常")
    void sessionCreateRequest_getterSetter() {
        SessionCreateRequest request = new SessionCreateRequest();
        request.setUserId("u1");
        request.setTitle("测试");
        request.setModel("deepseek-chat");

        assertEquals("u1", request.getUserId());
        assertEquals("测试", request.getTitle());
        assertEquals("deepseek-chat", request.getModel());
    }

    @Test
    @DisplayName("RagChatRequest - getter/setter正常")
    void ragChatRequest_getterSetter() {
        RagChatRequest request = new RagChatRequest();
        request.setQuestion("问题");
        request.setKnowledgeBaseId("kb1");
        request.setSessionId("s1");
        request.setTopK(3);

        assertEquals("问题", request.getQuestion());
        assertEquals("kb1", request.getKnowledgeBaseId());
        assertEquals("s1", request.getSessionId());
        assertEquals(3, request.getTopK());
    }

    @Test
    @DisplayName("VectorSearchRequest - getter/setter正常")
    void vectorSearchRequest_getterSetter() {
        VectorSearchRequest request = new VectorSearchRequest();
        request.setQuery("查询");
        request.setKnowledgeBaseId("kb1");
        request.setTopK(10);
        request.setSimilarityThreshold(0.8);

        assertEquals("查询", request.getQuery());
        assertEquals("kb1", request.getKnowledgeBaseId());
        assertEquals(10, request.getTopK());
        assertEquals(0.8, request.getSimilarityThreshold());
    }
}
