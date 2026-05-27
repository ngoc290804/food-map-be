package com.doan.backend.modules.chatbot.dto.request;

import jakarta.validation.constraints.NotBlank;
import java.math.BigDecimal;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChatbotAskRequest {

    @NotBlank(message = "Cau hoi khong duoc de trong")
    private String question;

    private BigDecimal latitude;

    private BigDecimal longitude;
}
