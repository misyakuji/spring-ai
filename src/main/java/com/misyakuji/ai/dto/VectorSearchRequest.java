package com.misyakuji.ai.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class VectorSearchRequest {

    @NotBlank(message = "query 不能为空")
    private String query;

    @NotBlank(message = "knowledgeBaseId 不能为空")
    private String knowledgeBaseId;

    private Integer topK = 5;
    private Double similarityThreshold = 0.7;
}
