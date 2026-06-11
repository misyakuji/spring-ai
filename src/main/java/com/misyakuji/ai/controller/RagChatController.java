package com.misyakuji.ai.controller;

import com.misyakuji.ai.dto.RagChatRequest;
import com.misyakuji.ai.service.RagChatService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/api/v1/rag")
@RequiredArgsConstructor
public class RagChatController {

    private final RagChatService ragChatService;

    @PostMapping(value = "/chat", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> ragChat(@Valid @RequestBody RagChatRequest request) {
        return ragChatService.ragChat(request);
    }
}
