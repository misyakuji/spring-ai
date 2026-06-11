package com.misyakuji.ai.memory;

import com.misyakuji.ai.entity.ChatMessageEntity;
import com.misyakuji.ai.repository.ChatMessageRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ListOperations;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JdbcChatMemoryTest {

    @Mock
    private ChatMessageRepository messageRepo;

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private ListOperations<String, String> listOperations;

    @InjectMocks
    private JdbcChatMemory jdbcChatMemory;

    @BeforeEach
    void setUp() {
        lenient().when(redisTemplate.opsForList()).thenReturn(listOperations);
    }

    @Test
    @DisplayName("add - 保存消息到数据库")
    void add_shouldSaveToDatabase() {
        List<Message> messages = List.of(
                new UserMessage("你好"),
                new AssistantMessage("你好！")
        );

        jdbcChatMemory.add("session-1", messages);

        verify(messageRepo, times(2)).save(any(ChatMessageEntity.class));
        verify(redisTemplate).delete("chat:mem:session-1");
    }

    @Test
    @DisplayName("add - 保存单条消息")
    void add_singleMessage() {
        jdbcChatMemory.add("session-1", List.of(new UserMessage("测试")));

        verify(messageRepo, times(1)).save(any(ChatMessageEntity.class));
    }

    @Test
    @DisplayName("get - 缓存命中时直接返回")
    void get_cacheHit_shouldReturnFromCache() {
        when(listOperations.range("chat:mem:session-1", 0, -1))
                .thenReturn(List.of("user|||你好", "assistant|||你好！"));

        List<Message> messages = jdbcChatMemory.get("session-1");

        assertEquals(2, messages.size());
        assertTrue(messages.get(0) instanceof UserMessage);
        assertTrue(messages.get(1) instanceof AssistantMessage);
        assertEquals("你好", messages.get(0).getText());
        assertEquals("你好！", messages.get(1).getText());

        verify(messageRepo, never()).findBySessionIdOrderByCreatedAtAsc(anyString());
    }

    @Test
    @DisplayName("get - 缓存未命中时查询数据库")
    void get_cacheMiss_shouldQueryDatabase() {
        when(listOperations.range("chat:mem:session-1", 0, -1))
                .thenReturn(null);

        ChatMessageEntity entity1 = new ChatMessageEntity();
        entity1.setRole("user");
        entity1.setContent("你好");

        ChatMessageEntity entity2 = new ChatMessageEntity();
        entity2.setRole("assistant");
        entity2.setContent("你好！");

        when(messageRepo.findBySessionIdOrderByCreatedAtAsc("session-1"))
                .thenReturn(List.of(entity1, entity2));

        List<Message> messages = jdbcChatMemory.get("session-1");

        assertEquals(2, messages.size());
        verify(listOperations, times(2)).rightPush(eq("chat:mem:session-1"), anyString());
    }

    @Test
    @DisplayName("clear - 清除数据库和缓存")
    void clear_shouldDeleteFromDbAndCache() {
        jdbcChatMemory.clear("session-1");

        verify(messageRepo).deleteBySessionId("session-1");
        verify(redisTemplate).delete("chat:mem:session-1");
    }

    @Test
    @DisplayName("get - 空缓存返回空列表")
    void get_emptyCacheAndDb_shouldReturnEmptyList() {
        when(listOperations.range("chat:mem:session-1", 0, -1))
                .thenReturn(null);
        when(messageRepo.findBySessionIdOrderByCreatedAtAsc("session-1"))
                .thenReturn(List.of());

        List<Message> messages = jdbcChatMemory.get("session-1");

        assertNotNull(messages);
        assertTrue(messages.isEmpty());
    }

    @Test
    @DisplayName("get - 缓存读取异常时降级到数据库")
    void get_cacheReadException_shouldFallbackToDb() {
        when(listOperations.range("chat:mem:session-1", 0, -1))
                .thenThrow(new RuntimeException("Redis连接失败"));

        ChatMessageEntity entity = new ChatMessageEntity();
        entity.setRole("user");
        entity.setContent("你好");

        when(messageRepo.findBySessionIdOrderByCreatedAtAsc("session-1"))
                .thenReturn(List.of(entity));

        List<Message> messages = jdbcChatMemory.get("session-1");

        assertEquals(1, messages.size());
        assertEquals("你好", messages.get(0).getText());
    }

    @Test
    @DisplayName("add - 缓存写入异常不影响数据库保存")
    void add_cacheWriteException_shouldNotAffectDb() {
        doThrow(new RuntimeException("Redis写入失败"))
                .when(redisTemplate).delete(anyString());

        jdbcChatMemory.add("session-1", List.of(new UserMessage("测试")));

        verify(messageRepo, times(1)).save(any(ChatMessageEntity.class));
    }
}
