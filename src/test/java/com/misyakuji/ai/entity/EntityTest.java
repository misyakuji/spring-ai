package com.misyakuji.ai.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class EntityTest {

    @Test
    @DisplayName("ChatSession - PrePersist设置时间戳")
    void chatSession_prePersist_setsTimestamps() {
        ChatSession session = new ChatSession();
        session.setId("test-id");
        session.setUserId("user-1");
        session.setTitle("测试");

        session.prePersist();

        assertNotNull(session.getCreatedAt());
        assertNotNull(session.getUpdatedAt());
    }

    @Test
    @DisplayName("ChatSession - PreUpdate更新updatedAt")
    void chatSession_preUpdate_updatesUpdatedAt() {
        ChatSession session = new ChatSession();
        session.prePersist();
        LocalDateTime createdAt = session.getCreatedAt();

        // 模拟延迟
        session.preUpdate();

        assertNotNull(session.getUpdatedAt());
        assertTrue(session.getUpdatedAt().isAfter(createdAt) || session.getUpdatedAt().isEqual(createdAt));
    }

    @Test
    @DisplayName("ChatMessageEntity - PrePersist设置时间戳")
    void chatMessageEntity_prePersist_setsTimestamp() {
        ChatMessageEntity message = new ChatMessageEntity();
        message.setSessionId("session-1");
        message.setRole("user");
        message.setContent("你好");

        message.prePersist();

        assertNotNull(message.getCreatedAt());
    }

    @Test
    @DisplayName("KnowledgeBase - PrePersist设置时间戳")
    void knowledgeBase_prePersist_setsTimestamp() {
        KnowledgeBase kb = new KnowledgeBase();
        kb.setId("kb-1");
        kb.setName("测试知识库");

        kb.prePersist();

        assertNotNull(kb.getCreatedAt());
    }

    @Test
    @DisplayName("KnowledgeDocument - PrePersist设置时间戳")
    void knowledgeDocument_prePersist_setsTimestamp() {
        KnowledgeDocument doc = new KnowledgeDocument();
        doc.setId("doc-1");
        doc.setKnowledgeBaseId("kb-1");
        doc.setFileName("test.pdf");

        doc.prePersist();

        assertNotNull(doc.getCreatedAt());
    }

    @Test
    @DisplayName("KnowledgeDocument - 默认状态为PROCESSING")
    void knowledgeDocument_defaultStatus() {
        KnowledgeDocument doc = new KnowledgeDocument();

        assertEquals("PROCESSING", doc.getStatus());
        assertEquals(Integer.valueOf(0), doc.getChunkCount());
    }
}
