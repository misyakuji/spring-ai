package com.misyakuji.ai.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "knowledge_documents")
@Getter
@Setter
public class KnowledgeDocument {

    @Id
    @Column(length = 64)
    private String id;

    @Column(name = "knowledge_base_id", nullable = false, length = 64)
    private String knowledgeBaseId;

    @Column(name = "file_name", nullable = false, length = 512)
    private String fileName;

    @Column(name = "file_type", length = 16)
    private String fileType;

    @Column(name = "file_size")
    private Long fileSize;

    @Column(name = "chunk_count")
    private Integer chunkCount = 0;

    @Column(length = 16)
    private String status = "PROCESSING";

    @Column(name = "error_msg", columnDefinition = "TEXT")
    private String errorMsg;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        createdAt = LocalDateTime.now();
    }
}
