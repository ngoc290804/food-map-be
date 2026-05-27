package com.doan.backend.modules.chatbot.service;

import com.doan.backend.config.OpenAiProperties;
import com.fasterxml.jackson.databind.JsonNode;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Service
@RequiredArgsConstructor
public class OpenAiChatClient {

    private final OpenAiProperties properties;
    private final RestClient.Builder restClientBuilder;

    public boolean isConfigured() {
        return properties.getApiKey() != null && !properties.getApiKey().isBlank();
    }

    public String generateAnswer(String question, String restaurantContext) {
        if (!isConfigured()) {
            return null;
        }

        RestClient restClient = restClientBuilder
                .baseUrl(properties.getBaseUrl())
                .defaultHeader("Authorization", "Bearer " + properties.getApiKey())
                .defaultHeader("Content-Type", "application/json")
                .build();

        Map<String, Object> body = Map.of(
                "model", properties.getModel(),
                "input", List.of(
                        Map.of(
                                "role", "system",
                                "content", """
                                        Bạn là chatbot tư vấn quán ăn cho ứng dụng Food Map.
                                        Trả lời ngắn gọn bằng tiếng Việt, thân thiện, ưu tiên đúng dữ liệu quán được cung cấp.
                                        Nếu không có quán phù hợp trong dữ liệu, hãy nói rõ và gợi ý người dùng nhập món, khu vực hoặc loại quán cụ thể hơn.
                                        Không tự bịa tên quán, địa chỉ hoặc giá.
                                        """
                        ),
                        Map.of(
                                "role", "user",
                                "content", "Câu hỏi: " + question + "\n\nDữ liệu quán phù hợp:\n" + restaurantContext
                        )
                )
        );

        try {
            JsonNode response = restClient.post()
                    .uri("/v1/responses")
                    .body(body)
                    .retrieve()
                    .body(JsonNode.class);
            return extractText(response);
        } catch (RestClientException ex) {
            return null;
        }
    }

    private String extractText(JsonNode response) {
        if (response == null) {
            return null;
        }
        JsonNode outputText = response.get("output_text");
        if (outputText != null && outputText.isTextual() && !outputText.asText().isBlank()) {
            return outputText.asText();
        }

        JsonNode output = response.get("output");
        if (output == null || !output.isArray()) {
            return null;
        }
        StringBuilder text = new StringBuilder();
        for (JsonNode item : output) {
            JsonNode content = item.get("content");
            if (content == null || !content.isArray()) {
                continue;
            }
            for (JsonNode contentItem : content) {
                JsonNode textNode = contentItem.get("text");
                if (textNode != null && textNode.isTextual()) {
                    text.append(textNode.asText());
                }
            }
        }
        return text.isEmpty() ? null : text.toString();
    }
}
