package com.doan.backend.modules.chatbot.service.impl;

import com.doan.backend.modules.chatbot.dto.request.ChatbotAskRequest;
import com.doan.backend.modules.chatbot.dto.response.ChatHistoryResponse;
import com.doan.backend.modules.chatbot.dto.response.ChatbotResponse;
import com.doan.backend.modules.chatbot.service.ChatbotService;
import com.doan.backend.modules.restaurant.service.RestaurantService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ChatbotServiceImpl implements ChatbotService {

    private final RestaurantService restaurantService;

    @Override
    public ChatbotResponse ask(ChatbotAskRequest request) {
        var matches = restaurantService.search(request.getQuestion(), 0, 5).getItems();
        String answer = matches.isEmpty()
                ? "Mình chưa tìm thấy quán phù hợp, bạn thử mô tả rõ hơn về món, vị hoặc khu vực."
                : "Mình gợi ý cho bạn một số quán phù hợp với nội dung bạn vừa hỏi.";

        return ChatbotResponse.builder()
                .answer(answer)
                .cuaHangs(matches)
                .build();
    }

    @Override
    public List<ChatHistoryResponse> getHistory() {
        return List.of();
    }
}
