package com.misyakuji.ai.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class KnowledgeBaseResponse {

    private String id;
    private String name;
    private String description;
    private String embedModel;
    private LocalDateTime createdAt;
}
