package com.doan.backend.modules.chatbot.repository;

import com.doan.backend.modules.chatbot.entity.ChatSession;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChatSessionRepository extends JpaRepository<ChatSession, UUID> {

    List<ChatSession> findByUserIdOrderByUpdatedAtDesc(UUID userId);
}
