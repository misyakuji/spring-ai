package com.misyakuji.ai.repository;

import com.misyakuji.ai.entity.ChatMessageEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessageEntity, Long> {

    List<ChatMessageEntity> findBySessionIdOrderByCreatedAtAsc(String sessionId);

    @Query("SELECT m FROM ChatMessageEntity m WHERE m.sessionId = :sessionId ORDER BY m.createdAt DESC")
    List<ChatMessageEntity> findRecentBySessionId(String sessionId, org.springframework.data.domain.Pageable pageable);

    @Modifying
    void deleteBySessionId(String sessionId);
}
