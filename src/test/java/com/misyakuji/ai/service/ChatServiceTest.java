package com.misyakuji.ai.service;

import com.misyakuji.ai.dto.ChatRequest;
import com.misyakuji.ai.dto.MessageResponse;
import com.misyakuji.ai.entity.ChatMessageEntity;
import com.misyakuji.ai.repository.ChatMessageRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ChatServiceTest {

    @Mock
    private ChatMessageRepository messageRepo;

    @Mock
    private org.springframework.ai.chat.client.ChatClient chatClient;

    @InjectMocks
    private ChatService chatService;

    private ChatRequest chatRequest;

    @BeforeEach
    void setUp() {
        chatRequest = new ChatRequest();
        chatRequest.setSessionId("test-session");
        chatRequest.setMessage("你好");
    }

    private ChatMessageEntity createMessage(String role, String content) {
        ChatMessageEntity msg = new ChatMessageEntity();
        msg.setRole(role);
        msg.setContent(content);
        msg.prePersist();
        return msg;
    }

    @Test
    @DisplayName("获取聊天历史 - 返回消息列表")
    void getHistory_shouldReturnMessageList() {
        ChatMessageEntity msg1 = createMessage("user", "你好");
        ChatMessageEntity msg2 = createMessage("assistant", "你好！有什么可以帮助你的？");

        when(messageRepo.findBySessionIdOrderByCreatedAtAsc("test-session"))
                .thenReturn(List.of(msg1, msg2));

        List<MessageResponse> history = chatService.getHistory("test-session", 10);

        assertEquals(2, history.size());
        assertEquals("user", history.get(0).getRole());
        assertEquals("你好", history.get(0).getContent());
        assertEquals("assistant", history.get(1).getRole());
        assertEquals("你好！有什么可以帮助你的？", history.get(1).getContent());
    }

    @Test
    @DisplayName("获取聊天历史 - 超出限制时截断")
    void getHistory_shouldTruncateWhenExceedingLimit() {
        ChatMessageEntity msg1 = createMessage("user", "msg1");
        ChatMessageEntity msg2 = createMessage("assistant", "msg2");
        ChatMessageEntity msg3 = createMessage("user", "msg3");

        when(messageRepo.findBySessionIdOrderByCreatedAtAsc("test-session"))
                .thenReturn(List.of(msg1, msg2, msg3));

        List<MessageResponse> history = chatService.getHistory("test-session", 2);

        assertEquals(2, history.size());
        assertEquals("msg2", history.get(0).getContent());
        assertEquals("msg3", history.get(1).getContent());
    }

    @Test
    @DisplayName("获取聊天历史 - 空会话返回空列表")
    void getHistory_emptySession() {
        when(messageRepo.findBySessionIdOrderByCreatedAtAsc("empty-session"))
                .thenReturn(List.of());

        List<MessageResponse> history = chatService.getHistory("empty-session", 10);

        assertNotNull(history);
        assertTrue(history.isEmpty());
    }
}
