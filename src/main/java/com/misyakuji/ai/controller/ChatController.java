package com.misyakuji.ai.controller;

import com.misyakuji.ai.dto.*;
import com.misyakuji.ai.service.ChatService;
import com.misyakuji.ai.service.SessionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.util.List;

@RestController
@RequestMapping("/api/v1/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;
    private final SessionService sessionService;

    @PostMapping("/sessions")
    public SessionResponse createSession(@Valid @RequestBody SessionCreateRequest request) {
        return sessionService.createSession(request);
    }

    @GetMapping("/sessions")
    public List<SessionResponse> listSessions(@RequestParam String userId) {
        return sessionService.listSessions(userId);
    }

    @DeleteMapping("/sessions/{id}")
    public void deleteSession(@PathVariable String id) {
        sessionService.deleteSession(id);
    }

    @PostMapping(value = "/completions", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> chat(@Valid @RequestBody ChatRequest request) {
        return chatService.streamChat(request);
    }

    @GetMapping("/sessions/{id}/messages")
    public List<MessageResponse> getHistory(
            @PathVariable String id,
            @RequestParam(defaultValue = "20") int limit) {
        return chatService.getHistory(id, limit);
    }
}
