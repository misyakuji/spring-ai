package com.misyakuji.ai.service;

import com.misyakuji.ai.dto.SessionCreateRequest;
import com.misyakuji.ai.dto.SessionResponse;
import com.misyakuji.ai.entity.ChatSession;
import com.misyakuji.ai.repository.ChatMessageRepository;
import com.misyakuji.ai.repository.ChatSessionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SessionServiceTest {

    @Mock
    private ChatSessionRepository sessionRepo;

    @Mock
    private ChatMessageRepository messageRepo;

    @InjectMocks
    private SessionService sessionService;

    private ChatSession testSession;

    @BeforeEach
    void setUp() {
        testSession = new ChatSession();
        testSession.setId(UUID.randomUUID().toString());
        testSession.setUserId("test-user");
        testSession.setTitle("测试会话");
        testSession.setModel("deepseek-chat");
        testSession.prePersist();
    }

    @Test
    @DisplayName("创建会话 - 返回包含ID的响应")
    void createSession_shouldReturnSessionWithId() {
        SessionCreateRequest request = new SessionCreateRequest();
        request.setUserId("test-user");
        request.setTitle("新会话");

        when(sessionRepo.saveAndFlush(any(ChatSession.class))).thenAnswer(invocation -> {
            ChatSession session = invocation.getArgument(0);
            session.prePersist();
            return session;
        });

        SessionResponse response = sessionService.createSession(request);

        assertNotNull(response);
        assertNotNull(response.getId());
        assertEquals("test-user", response.getUserId());
        assertEquals("新会话", response.getTitle());
        verify(sessionRepo, times(1)).saveAndFlush(any(ChatSession.class));
    }

    @Test
    @DisplayName("创建会话 - 无标题时使用默认标题")
    void createSession_shouldUseDefaultTitle() {
        SessionCreateRequest request = new SessionCreateRequest();
        request.setUserId("test-user");

        when(sessionRepo.saveAndFlush(any(ChatSession.class))).thenAnswer(invocation -> {
            ChatSession session = invocation.getArgument(0);
            session.prePersist();
            return session;
        });

        SessionResponse response = sessionService.createSession(request);

        assertEquals("新会话", response.getTitle());
    }

    @Test
    @DisplayName("列出会话 - 按更新时间倒序返回")
    void listSessions_shouldReturnSessionsByUpdatedAtDesc() {
        ChatSession session1 = new ChatSession();
        session1.setId("1");
        session1.setUserId("test-user");
        session1.prePersist();

        ChatSession session2 = new ChatSession();
        session2.setId("2");
        session2.setUserId("test-user");
        session2.prePersist();

        when(sessionRepo.findByUserIdOrderByUpdatedAtDesc("test-user"))
                .thenReturn(List.of(session2, session1));

        List<SessionResponse> sessions = sessionService.listSessions("test-user");

        assertEquals(2, sessions.size());
        assertEquals("2", sessions.get(0).getId());
        assertEquals("1", sessions.get(1).getId());
    }

    @Test
    @DisplayName("获取会话 - 存在时返回会话")
    void getSession_shouldReturnSessionIfExists() {
        when(sessionRepo.findById(testSession.getId())).thenReturn(Optional.of(testSession));

        ChatSession result = sessionService.getSession(testSession.getId());

        assertNotNull(result);
        assertEquals(testSession.getId(), result.getId());
    }

    @Test
    @DisplayName("获取会话 - 不存在时抛出异常")
    void getSession_shouldThrowWhenNotFound() {
        when(sessionRepo.findById("nonexistent")).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> sessionService.getSession("nonexistent"));
    }

    @Test
    @DisplayName("删除会话 - 先删消息再删会话")
    void deleteSession_shouldDeleteMessagesThenSession() {
        String sessionId = testSession.getId();

        sessionService.deleteSession(sessionId);

        verify(messageRepo, times(1)).deleteBySessionId(sessionId);
        verify(sessionRepo, times(1)).deleteById(sessionId);
    }
}
