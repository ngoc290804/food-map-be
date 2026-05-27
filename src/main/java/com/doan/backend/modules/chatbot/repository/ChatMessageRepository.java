package com.doan.backend.modules.chatbot.repository;

import com.doan.backend.modules.chatbot.entity.ChatMessage;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, UUID> {

    List<ChatMessage> findBySessionIdOrderByCreatedAtAsc(UUID sessionId);

    Optional<ChatMessage> findFirstBySessionIdAndRoleOrderByCreatedAtAsc(UUID sessionId, String role);
}
