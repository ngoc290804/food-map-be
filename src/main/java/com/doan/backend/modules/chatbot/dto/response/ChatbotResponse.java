package com.doan.backend.modules.chatbot.dto.response;

import com.doan.backend.modules.restaurant.vo.CuaHangVo;
import java.util.List;
import java.util.UUID;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ChatbotResponse {
    private final UUID sessionId;
    private final String answer;
    private final List<CuaHangVo> cuaHangs;
}
