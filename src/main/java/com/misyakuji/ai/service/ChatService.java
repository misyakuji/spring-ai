package com.misyakuji.ai.service;

import com.misyakuji.ai.dto.ChatRequest;
import com.misyakuji.ai.dto.MessageResponse;
import com.misyakuji.ai.entity.ChatMessageEntity;
import com.misyakuji.ai.repository.ChatMessageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.List;

import static org.springframework.ai.chat.memory.ChatMemory.CONVERSATION_ID;

@Service
@RequiredArgsConstructor
public class ChatService {

    private final ChatClient chatClient;
    private final ChatMessageRepository messageRepo;

    public Flux<String> streamChat(ChatRequest request) {
        return chatClient.prompt()
                .user(request.getMessage())
                .advisors(a -> a.param(CONVERSATION_ID, request.getSessionId()))
                .stream()
                .content();
    }

    public String chat(ChatRequest request) {
        return chatClient.prompt()
                .user(request.getMessage())
                .advisors(a -> a.param(CONVERSATION_ID, request.getSessionId()))
                .call()
                .content();
    }

    public List<MessageResponse> getHistory(String sessionId, int limit) {
        List<ChatMessageEntity> entities = messageRepo.findBySessionIdOrderByCreatedAtAsc(sessionId);
        int start = Math.max(0, entities.size() - limit);
        return entities.subList(start, entities.size()).stream()
                .map(e -> new MessageResponse(
                        e.getRole(),
                        e.getContent(),
                        e.getCreatedAt() != null ? e.getCreatedAt().atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli() : null))
                .toList();
    }
}
