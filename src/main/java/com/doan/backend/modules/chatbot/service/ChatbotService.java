package com.doan.backend.modules.chatbot.service;

import com.doan.backend.modules.chatbot.dto.request.ChatbotAskRequest;
import com.doan.backend.modules.chatbot.dto.response.ChatHistoryResponse;
import com.doan.backend.modules.chatbot.dto.response.ChatbotResponse;
import java.util.List;

public interface ChatbotService {
    ChatbotResponse ask(ChatbotAskRequest request);

    List<ChatHistoryResponse> getHistory();
}
