package com.doan.backend.modules.chatbot.entity;

import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChatHistory {

    private UUID id;

    private String question;

    private String answer;

    private LocalDateTime createdAt;
}
