package com.misyakuji.ai.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class SessionResponse {

    private String id;
    private String userId;
    private String title;
    private String model;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
