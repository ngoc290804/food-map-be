package com.doan.backend.modules.chatbot.service;

import com.doan.backend.config.OpenAiProperties;
import com.doan.backend.modules.chatbot.dto.request.ChatbotAskRequest;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Service
@RequiredArgsConstructor
public class OpenAiChatClient {

    private static final String INSTRUCTIONS = """
            Ban la FoodMap AI, tro ly tu van quan an bang tieng Viet.
            Luon dua tren du lieu that tu tool, khong tu bia ten quan, dia chi, diem danh gia hoac menu.
            Neu nguoi dung hoi tim/goi y/so sanh quan, hay goi tool search_restaurants truoc.
            Cau tra loi ngan gon, than thien, neu co quan phu hop thi neu ly do chon tung quan.
            Neu thieu thong tin quan trong nhu khu vuc, mon an, muc gia, hay hoi lai mot cau ro rang.
            """;

    private final OpenAiProperties properties;
    private final RestClient.Builder restClientBuilder;
    private final ObjectMapper objectMapper;

    public boolean isConfigured() {
        return properties.getApiKey() != null && !properties.getApiKey().isBlank();
    }

    public AiChatResult generateRecommendation(ChatbotAskRequest request, ToolExecutor toolExecutor) {
        if (!isConfigured()) {
            return null;
        }

        RestClient restClient = restClientBuilder
                .baseUrl(properties.getBaseUrl())
                .defaultHeader("Authorization", "Bearer " + properties.getApiKey())
                .defaultHeader("Content-Type", "application/json")
                .build();

        List<Object> input = new ArrayList<>();
        input.add(Map.of(
                "role", "user",
                "content", buildUserContent(request)
        ));

        try {
            JsonNode firstResponse = createResponse(restClient, input, false);
            List<JsonNode> toolCalls = findFunctionCalls(firstResponse);

            if (toolCalls.isEmpty()) {
                return parseAiResult(extractText(firstResponse));
            }

            JsonNode output = firstResponse.get("output");
            if (output != null && output.isArray()) {
                output.forEach(input::add);
            }

            for (JsonNode toolCall : toolCalls) {
                String outputText = toolExecutor.execute(
                        textValue(toolCall.get("name")),
                        parseArguments(toolCall.get("arguments"))
                );
                input.add(Map.of(
                        "type", "function_call_output",
                        "call_id", textValue(toolCall.get("call_id")),
                        "output", outputText
                ));
            }

            JsonNode finalResponse = createResponse(restClient, input, true);
            return parseAiResult(extractText(finalResponse));
        } catch (RestClientException | IllegalArgumentException ex) {
            return null;
        }
    }

    private JsonNode createResponse(RestClient restClient, List<Object> input, boolean structuredOutput) {
        Map<String, Object> body = structuredOutput
                ? Map.of(
                        "model", properties.getModel(),
                        "instructions", INSTRUCTIONS,
                        "tools", tools(),
                        "input", input,
                        "text", Map.of("format", responseFormat())
                )
                : Map.of(
                        "model", properties.getModel(),
                        "instructions", INSTRUCTIONS,
                        "tools", tools(),
                        "input", input
                );

        return restClient.post()
                .uri("/v1/responses")
                .body(body)
                .retrieve()
                .body(JsonNode.class);
    }

    private String buildUserContent(ChatbotAskRequest request) {
        StringBuilder content = new StringBuilder();
        content.append("Cau hoi nguoi dung: ").append(request.getQuestion());
        if (request.getLatitude() != null && request.getLongitude() != null) {
            content.append("\nVi tri nguoi dung: latitude=")
                    .append(request.getLatitude())
                    .append(", longitude=")
                    .append(request.getLongitude());
        }
        return content.toString();
    }

    private List<Map<String, Object>> tools() {
        return List.of(Map.of(
                "type", "function",
                "name", "search_restaurants",
                "description", "Tim quan an trong he thong FoodMap theo y dinh cua nguoi dung.",
                "parameters", Map.of(
                        "type", "object",
                        "properties", Map.of(
                                "query", Map.of(
                                        "type", "string",
                                        "description", "Mon an, ten quan, nhu cau hoac tu khoa chinh. De rong neu chi can goi y chung."
                                ),
                                "location", Map.of(
                                        "type", "string",
                                        "description", "Khu vuc/dia chi nguoi dung nhac den. De rong neu khong co."
                                ),
                                "category", Map.of(
                                        "type", "string",
                                        "enum", List.of("TRANG_CHU", "QUAN_AN", "QUAN_NHAU", "TRA_SUA_DO_UONG", "CAFE", "BANH_NGOT_AN_VAT", "YEU_THICH", ""),
                                        "description", "Loai cua hang neu suy ra duoc."
                                ),
                                "minRating", Map.of(
                                        "type", "number",
                                        "description", "Diem danh gia toi thieu, tu 0 den 5."
                                ),
                                "openNow", Map.of(
                                        "type", "boolean",
                                        "description", "true neu nguoi dung can quan dang mo cua."
                                ),
                                "sortBy", Map.of(
                                        "type", "string",
                                        "enum", List.of("relevance", "rating", "distance"),
                                        "description", "Cach sap xep phu hop voi cau hoi."
                                ),
                                "limit", Map.of(
                                        "type", "integer",
                                        "minimum", 1,
                                        "maximum", 5,
                                        "description", "So quan can tra ve."
                                )
                        ),
                        "required", List.of("query", "location", "category", "minRating", "openNow", "sortBy", "limit"),
                        "additionalProperties", false
                )
        ));
    }

    private Map<String, Object> responseFormat() {
        return Map.of(
                "type", "json_schema",
                "name", "foodmap_chatbot_response",
                "strict", true,
                "schema", Map.of(
                        "type", "object",
                        "properties", Map.of(
                                "answer", Map.of("type", "string"),
                                "restaurantIds", Map.of(
                                        "type", "array",
                                        "items", Map.of("type", "string")
                                ),
                                "needMoreInfo", Map.of("type", "boolean"),
                                "followUpQuestion", Map.of("type", List.of("string", "null"))
                        ),
                        "required", List.of("answer", "restaurantIds", "needMoreInfo", "followUpQuestion"),
                        "additionalProperties", false
                )
        );
    }

    private List<JsonNode> findFunctionCalls(JsonNode response) {
        JsonNode output = response == null ? null : response.get("output");
        if (output == null || !output.isArray()) {
            return List.of();
        }
        List<JsonNode> calls = new ArrayList<>();
        for (JsonNode item : output) {
            if ("function_call".equals(textValue(item.get("type")))) {
                calls.add(item);
            }
        }
        return calls;
    }

    private JsonNode parseArguments(JsonNode argumentsNode) {
        try {
            String arguments = textValue(argumentsNode);
            return arguments.isBlank() ? objectMapper.createObjectNode() : objectMapper.readTree(arguments);
        } catch (JsonProcessingException ex) {
            return objectMapper.createObjectNode();
        }
    }

    private AiChatResult parseAiResult(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            JsonNode root = objectMapper.readTree(value);
            String answer = textValue(root.get("answer"));
            boolean needMoreInfo = root.path("needMoreInfo").asBoolean(false);
            String followUpQuestion = root.path("followUpQuestion").isNull()
                    ? null
                    : textValue(root.get("followUpQuestion"));
            LinkedHashSet<UUID> restaurantIds = new LinkedHashSet<>();
            JsonNode ids = root.get("restaurantIds");
            if (ids != null && ids.isArray()) {
                for (JsonNode idNode : ids) {
                    try {
                        restaurantIds.add(UUID.fromString(idNode.asText()));
                    } catch (IllegalArgumentException ignored) {
                        // Ignore malformed ids returned by the model.
                    }
                }
            }
            return new AiChatResult(answer, new ArrayList<>(restaurantIds), needMoreInfo, followUpQuestion);
        } catch (JsonProcessingException ex) {
            return new AiChatResult(value, List.of(), false, null);
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

    private String textValue(JsonNode node) {
        return node == null || node.isNull() ? "" : node.asText();
    }

    @FunctionalInterface
    public interface ToolExecutor {
        String execute(String name, JsonNode arguments);
    }

    public record AiChatResult(
            String answer,
            List<UUID> restaurantIds,
            boolean needMoreInfo,
            String followUpQuestion
    ) {
    }
}
