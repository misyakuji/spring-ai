package com.misyakuji.ai.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RagChatRequest {

    @NotBlank(message = "question 不能为空")
    private String question;

    @NotBlank(message = "knowledgeBaseId 不能为空")
    private String knowledgeBaseId;

    private String sessionId;
    private Integer topK = 5;
}
