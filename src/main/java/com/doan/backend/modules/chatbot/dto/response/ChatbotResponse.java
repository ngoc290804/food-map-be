package com.doan.backend.modules.chatbot.dto.response;

import com.doan.backend.modules.restaurant.dto.response.RestaurantResponse;
import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ChatbotResponse {
    private final String answer;
    private final List<RestaurantResponse> restaurants;
}
