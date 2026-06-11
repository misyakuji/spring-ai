package com.misyakuji.ai.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class SessionCreateRequest {

    @NotBlank(message = "userId 不能为空")
    private String userId;

    private String title;
    private String model;
}
