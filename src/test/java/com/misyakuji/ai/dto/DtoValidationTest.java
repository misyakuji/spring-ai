package com.misyakuji.ai.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class DtoValidationTest {

    private static Validator validator;

    @BeforeAll
    static void setUpValidator() {
        try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
            validator = factory.getValidator();
        }
    }

    @Test
    @DisplayName("ChatRequest - sessionId不能为空")
    void chatRequest_blankSessionId_shouldFail() {
        ChatRequest request = new ChatRequest();
        request.setSessionId("");
        request.setMessage("你好");

        Set<ConstraintViolation<ChatRequest>> violations = validator.validate(request);

        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("sessionId")));
    }

    @Test
    @DisplayName("ChatRequest - message不能为空")
    void chatRequest_blankMessage_shouldFail() {
        ChatRequest request = new ChatRequest();
        request.setSessionId("session-123");
        request.setMessage("");

        Set<ConstraintViolation<ChatRequest>> violations = validator.validate(request);

        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("message")));
    }

    @Test
    @DisplayName("ChatRequest - 有效数据通过验证")
    void chatRequest_validData_shouldPass() {
        ChatRequest request = new ChatRequest();
        request.setSessionId("session-123");
        request.setMessage("你好");

        Set<ConstraintViolation<ChatRequest>> violations = validator.validate(request);

        assertTrue(violations.isEmpty());
    }

    @Test
    @DisplayName("SessionCreateRequest - userId不能为空")
    void sessionCreateRequest_blankUserId_shouldFail() {
        SessionCreateRequest request = new SessionCreateRequest();
        request.setUserId("");

        Set<ConstraintViolation<SessionCreateRequest>> violations = validator.validate(request);

        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("userId")));
    }

    @Test
    @DisplayName("SessionCreateRequest - 有效数据通过验证")
    void sessionCreateRequest_validData_shouldPass() {
        SessionCreateRequest request = new SessionCreateRequest();
        request.setUserId("user-123");

        Set<ConstraintViolation<SessionCreateRequest>> violations = validator.validate(request);

        assertTrue(violations.isEmpty());
    }

    @Test
    @DisplayName("KnowledgeBaseCreateRequest - name不能为空")
    void knowledgeBaseCreateRequest_blankName_shouldFail() {
        KnowledgeBaseCreateRequest request = new KnowledgeBaseCreateRequest();
        request.setName("");

        Set<ConstraintViolation<KnowledgeBaseCreateRequest>> violations = validator.validate(request);

        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("name")));
    }

    @Test
    @DisplayName("KnowledgeBaseCreateRequest - 有效数据通过验证")
    void knowledgeBaseCreateRequest_validData_shouldPass() {
        KnowledgeBaseCreateRequest request = new KnowledgeBaseCreateRequest();
        request.setName("测试知识库");

        Set<ConstraintViolation<KnowledgeBaseCreateRequest>> violations = validator.validate(request);

        assertTrue(violations.isEmpty());
    }

    @Test
    @DisplayName("RagChatRequest - question不能为空")
    void ragChatRequest_blankQuestion_shouldFail() {
        RagChatRequest request = new RagChatRequest();
        request.setQuestion("");
        request.setKnowledgeBaseId("kb-123");

        Set<ConstraintViolation<RagChatRequest>> violations = validator.validate(request);

        assertFalse(violations.isEmpty());
    }

    @Test
    @DisplayName("RagChatRequest - knowledgeBaseId不能为空")
    void ragChatRequest_blankKnowledgeBaseId_shouldFail() {
        RagChatRequest request = new RagChatRequest();
        request.setQuestion("测试问题");
        request.setKnowledgeBaseId("");

        Set<ConstraintViolation<RagChatRequest>> violations = validator.validate(request);

        assertFalse(violations.isEmpty());
    }

    @Test
    @DisplayName("VectorSearchRequest - query不能为空")
    void vectorSearchRequest_blankQuery_shouldFail() {
        VectorSearchRequest request = new VectorSearchRequest();
        request.setQuery("");
        request.setKnowledgeBaseId("kb-123");

        Set<ConstraintViolation<VectorSearchRequest>> violations = validator.validate(request);

        assertFalse(violations.isEmpty());
    }

    @Test
    @DisplayName("VectorSearchRequest - 有效数据通过验证")
    void vectorSearchRequest_validData_shouldPass() {
        VectorSearchRequest request = new VectorSearchRequest();
        request.setQuery("测试查询");
        request.setKnowledgeBaseId("kb-123");

        Set<ConstraintViolation<VectorSearchRequest>> violations = validator.validate(request);

        assertTrue(violations.isEmpty());
    }
}
