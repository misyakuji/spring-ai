package com.misyakuji.ai.service;

import com.misyakuji.ai.dto.SessionCreateRequest;
import com.misyakuji.ai.dto.SessionResponse;
import com.misyakuji.ai.entity.ChatSession;
import com.misyakuji.ai.repository.ChatMessageRepository;
import com.misyakuji.ai.repository.ChatSessionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
public class SessionService {

    private final ChatSessionRepository sessionRepo;
    private final ChatMessageRepository messageRepo;

    public SessionResponse createSession(SessionCreateRequest request) {
        ChatSession session = new ChatSession();
        session.setId(UUID.randomUUID().toString());
        session.setUserId(request.getUserId());
        session.setTitle(request.getTitle() != null ? request.getTitle() : "新会话");
        session.setModel(request.getModel());
        sessionRepo.saveAndFlush(session);
        return toResponse(session);
    }

    public List<SessionResponse> listSessions(String userId) {
        return sessionRepo.findByUserIdOrderByUpdatedAtDesc(userId).stream()
                .map(this::toResponse)
                .toList();
    }

    public void deleteSession(String sessionId) {
        messageRepo.deleteBySessionId(sessionId);
        sessionRepo.deleteById(sessionId);
    }

    public ChatSession getSession(String sessionId) {
        return sessionRepo.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("会话不存在: " + sessionId));
    }

    private SessionResponse toResponse(ChatSession session) {
        SessionResponse resp = new SessionResponse();
        resp.setId(session.getId());
        resp.setUserId(session.getUserId());
        resp.setTitle(session.getTitle());
        resp.setModel(session.getModel());
        resp.setCreatedAt(session.getCreatedAt());
        resp.setUpdatedAt(session.getUpdatedAt());
        return resp;
    }
}
