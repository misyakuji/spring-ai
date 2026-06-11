package com.misyakuji.ai.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class KnowledgeDocumentResponse {

    private String id;
    private String knowledgeBaseId;
    private String fileName;
    private String fileType;
    private Long fileSize;
    private Integer chunkCount;
    private String status;
    private String errorMsg;
    private LocalDateTime createdAt;
}
