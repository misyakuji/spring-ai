package com.misyakuji.ai.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class KnowledgeBaseCreateRequest {

    @NotBlank(message = "name 不能为空")
    private String name;

    private String description;
    private String embedModel;
}
