package com.misyakuji.ai.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ChatRequest {

    @NotBlank(message = "sessionId 不能为空")
    private String sessionId;

    @NotBlank(message = "message 不能为空")
    private String message;

    private String userId;
}
