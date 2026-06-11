package com.misyakuji.ai.memory;

import com.misyakuji.ai.entity.ChatMessageEntity;
import com.misyakuji.ai.repository.ChatMessageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
@Slf4j
public class JdbcChatMemory implements ChatMemory {

    private static final String CACHE_PREFIX = "chat:mem:";
    private static final long CACHE_TTL_MINUTES = 30;

    private final ChatMessageRepository messageRepo;
    private final StringRedisTemplate redisTemplate;

    @Override
    public void add(String conversationId, List<Message> messages) {
        for (Message msg : messages) {
            ChatMessageEntity entity = new ChatMessageEntity();
            entity.setSessionId(conversationId);
            entity.setRole(extractRole(msg));
            entity.setContent(msg.getText());
            messageRepo.save(entity);
        }
        evictCache(conversationId);
        log.debug("Saved {} messages for conversation {}", messages.size(), conversationId);
    }

    @Override
    public List<Message> get(String conversationId) {
        List<Message> cached = getCachedMessages(conversationId);
        if (cached != null) {
            log.debug("Cache hit for conversation {}", conversationId);
            return cached;
        }

        List<ChatMessageEntity> entities = messageRepo.findBySessionIdOrderByCreatedAtAsc(conversationId);

        List<Message> messages = entities.stream()
                .map(this::toMessage)
                .toList();

        cacheMessages(conversationId, messages);
        return messages;
    }

    @Override
    public void clear(String conversationId) {
        messageRepo.deleteBySessionId(conversationId);
        evictCache(conversationId);
        log.debug("Cleared messages for conversation {}", conversationId);
    }

    private List<Message> getCachedMessages(String conversationId) {
        try {
            String cacheKey = CACHE_PREFIX + conversationId;
            List<String> cached = redisTemplate.opsForList().range(cacheKey, 0, -1);
            if (cached != null && !cached.isEmpty()) {
                return cached.stream()
                        .map(this::deserializeMessage)
                        .toList();
            }
        } catch (Exception e) {
            log.warn("Redis cache read failed for conversation {}", conversationId, e);
        }
        return null;
    }

    private void cacheMessages(String conversationId, List<Message> messages) {
        try {
            String cacheKey = CACHE_PREFIX + conversationId;
            redisTemplate.delete(cacheKey);
            for (Message msg : messages) {
                redisTemplate.opsForList().rightPush(cacheKey, serializeMessage(msg));
            }
            redisTemplate.expire(cacheKey, CACHE_TTL_MINUTES, TimeUnit.MINUTES);
        } catch (Exception e) {
            log.warn("Redis cache write failed for conversation {}", conversationId, e);
        }
    }

    private void evictCache(String conversationId) {
        try {
            redisTemplate.delete(CACHE_PREFIX + conversationId);
        } catch (Exception e) {
            log.warn("Redis cache eviction failed for conversation {}", conversationId, e);
        }
    }

    private String serializeMessage(Message msg) {
        return extractRole(msg) + "|||" + msg.getText();
    }

    private Message deserializeMessage(String data) {
        String[] parts = data.split("\\|\\|\\|", 2);
        if (parts.length != 2) return new UserMessage(data);
        return switch (parts[0]) {
            case "assistant" -> new AssistantMessage(parts[1]);
            case "system" -> new SystemMessage(parts[1]);
            default -> new UserMessage(parts[1]);
        };
    }

    private Message toMessage(ChatMessageEntity entity) {
        return switch (entity.getRole()) {
            case "assistant" -> new AssistantMessage(entity.getContent());
            case "system" -> new SystemMessage(entity.getContent());
            default -> new UserMessage(entity.getContent());
        };
    }

    private String extractRole(Message msg) {
        if (msg instanceof AssistantMessage) return "assistant";
        if (msg instanceof SystemMessage) return "system";
        return "user";
    }
}
