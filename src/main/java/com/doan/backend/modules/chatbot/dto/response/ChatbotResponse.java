package com.doan.backend.modules.chatbot.dto.response;

import com.doan.backend.modules.restaurant.vo.CuaHangVo;
import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ChatbotResponse {
    private final String answer;
    private final List<CuaHangVo> cuaHangs;
}
