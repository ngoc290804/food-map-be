package com.doan.backend.modules.chatbot.controller;

import com.doan.backend.common.dto.ApiResponse;
import com.doan.backend.modules.chatbot.dto.request.ChatbotAskRequest;
import com.doan.backend.modules.chatbot.dto.response.ChatHistoryResponse;
import com.doan.backend.modules.chatbot.dto.response.ChatbotResponse;
import com.doan.backend.modules.chatbot.service.ChatbotService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/chatbot")
@RequiredArgsConstructor
public class ChatbotController {

    private final ChatbotService chatbotService;

    @PostMapping("/ask")
    public ApiResponse<ChatbotResponse> ask(@Valid @RequestBody ChatbotAskRequest request) {
        return ApiResponse.success(chatbotService.ask(request));
    }

    @GetMapping("/history")
    public ApiResponse<List<ChatHistoryResponse>> history() {
        return ApiResponse.success(chatbotService.getHistory());
    }
}
