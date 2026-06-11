package com.misyakuji.ai.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class GlobalExceptionHandlerTest {

    @Test
    @DisplayName("GlobalExceptionHandler - 实例化正常")
    void instantiate_shouldWork() {
        GlobalExceptionHandler handler = new GlobalExceptionHandler();
        assertNotNull(handler);
    }
}
