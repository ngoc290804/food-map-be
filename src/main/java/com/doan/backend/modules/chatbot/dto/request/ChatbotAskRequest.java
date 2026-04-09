package com.doan.backend.modules.chatbot.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChatbotAskRequest {

    @NotBlank(message = "Câu hỏi không được để trống")
    private String question;
}
