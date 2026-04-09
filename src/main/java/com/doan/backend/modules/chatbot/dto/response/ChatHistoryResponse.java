package com.doan.backend.modules.chatbot.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ChatHistoryResponse {
    private final UUID id;
    private final String question;
    private final String answer;
    private final LocalDateTime createdAt;
}
