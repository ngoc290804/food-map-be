package com.doan.backend.modules.chatbot.service.impl;

import com.doan.backend.config.OpenAiProperties;
import com.doan.backend.modules.chatbot.dto.request.ChatbotAskRequest;
import com.doan.backend.modules.chatbot.dto.response.ChatHistoryResponse;
import com.doan.backend.modules.chatbot.dto.response.ChatbotResponse;
import com.doan.backend.modules.chatbot.entity.ChatMessage;
import com.doan.backend.modules.chatbot.entity.ChatSession;
import com.doan.backend.modules.chatbot.repository.ChatMessageRepository;
import com.doan.backend.modules.chatbot.repository.ChatSessionRepository;
import com.doan.backend.modules.chatbot.service.ChatbotService;
import com.doan.backend.modules.chatbot.service.OpenAiChatClient;
import com.doan.backend.modules.chatbot.service.OpenAiChatClient.AiChatResult;
import com.doan.backend.modules.menu.entity.MenuItem;
import com.doan.backend.modules.menu.repository.MenuItemRepository;
import com.doan.backend.modules.restaurant.service.RestaurantService;
import com.doan.backend.modules.restaurant.vo.CuaHangVo;
import com.doan.backend.modules.user.repository.UserRepository;
import com.doan.backend.security.SecurityUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.text.Normalizer;
import java.time.LocalTime;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ChatbotServiceImpl implements ChatbotService {

    private static final Pattern DIACRITICS_PATTERN = Pattern.compile("\\p{M}+");

    private final RestaurantService restaurantService;
    private final OpenAiChatClient openAiChatClient;
    private final OpenAiProperties openAiProperties;
    private final ChatSessionRepository chatSessionRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;
    private final MenuItemRepository menuItemRepository;

    @Override
    @Transactional
    public ChatbotResponse ask(ChatbotAskRequest request) {
        List<CuaHangVo> allRestaurants = restaurantService.findActiveForChatbot(
                Math.max(1, openAiProperties.getMaxRestaurants()));
        List<MenuItem> menuItems = menuItemRepository.findActiveRestaurantMenuItems();
        Map<UUID, CuaHangVo> restaurantIndex = allRestaurants.stream()
                .collect(Collectors.toMap(CuaHangVo::getId, item -> item, (left, right) -> left, LinkedHashMap::new));

        AiChatResult aiResult = openAiChatClient.generateRecommendation(
                request,
                (name, arguments) -> executeTool(name, arguments, request, allRestaurants, menuItems)
        );

        List<CuaHangVo> suggestedRestaurants = restaurantsFromAiResult(aiResult, restaurantIndex);
        if (suggestedRestaurants.isEmpty()) {
            suggestedRestaurants = searchRestaurantsByQuestion(request, allRestaurants, menuItems, 5);
        }

        String answer = buildAnswer(aiResult, suggestedRestaurants);
        ChatSession session = createSession(request.getQuestion());
        saveMessage(session, "user", request.getQuestion(), null);
        saveMessage(session, "assistant", answer, buildJsonData(suggestedRestaurants));

        return ChatbotResponse.builder()
                .sessionId(session.getId())
                .answer(answer)
                .cuaHangs(suggestedRestaurants)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ChatHistoryResponse> getHistory() {
        return SecurityUtils.getCurrentUser()
                .map(user -> chatSessionRepository.findByUserIdOrderByUpdatedAtDesc(user.getId()).stream()
                        .map(this::mapToHistory)
                        .toList())
                .orElse(List.of());
    }

    private String executeTool(
            String name,
            JsonNode arguments,
            ChatbotAskRequest request,
            List<CuaHangVo> restaurants,
            List<MenuItem> menuItems
    ) {
        if (!"search_restaurants".equals(name)) {
            return "{\"error\":\"Unknown tool\"}";
        }

        List<CuaHangVo> matches = searchRestaurantsByArguments(arguments, request, restaurants, menuItems);
        return serializeToolResult(matches, menuItems);
    }

    private List<CuaHangVo> restaurantsFromAiResult(AiChatResult aiResult, Map<UUID, CuaHangVo> restaurantIndex) {
        if (aiResult == null || aiResult.restaurantIds() == null) {
            return List.of();
        }
        return aiResult.restaurantIds().stream()
                .map(restaurantIndex::get)
                .filter(item -> item != null)
                .limit(5)
                .toList();
    }

    private String buildAnswer(AiChatResult aiResult, List<CuaHangVo> suggestedRestaurants) {
        if (aiResult != null && aiResult.answer() != null && !aiResult.answer().isBlank()) {
            if (!suggestedRestaurants.isEmpty() && isNoResultAnswer(aiResult.answer())) {
                return buildFallbackAnswer(suggestedRestaurants);
            }
            return aiResult.answer();
        }
        return buildFallbackAnswer(suggestedRestaurants);
    }

    private List<CuaHangVo> searchRestaurantsByArguments(
            JsonNode arguments,
            ChatbotAskRequest request,
            List<CuaHangVo> restaurants,
            List<MenuItem> menuItems
    ) {
        ToolCriteria criteria = buildCriteria(
                text(arguments, "query"),
                text(arguments, "location"),
                text(arguments, "category"),
                decimal(arguments, "minRating"),
                arguments.path("openNow").asBoolean(false),
                text(arguments, "sortBy"),
                intValue(arguments, "limit", 5),
                request.getLatitude(),
                request.getLongitude()
        );
        return searchRestaurants(criteria, restaurants, menuItems);
    }

    private List<CuaHangVo> searchRestaurantsByQuestion(
            ChatbotAskRequest request,
            List<CuaHangVo> restaurants,
            List<MenuItem> menuItems,
            int limit
    ) {
        return searchRestaurants(
                buildCriteria(request.getQuestion(), "", "", null, false, "relevance", limit,
                        request.getLatitude(), request.getLongitude()),
                restaurants,
                menuItems
        );
    }

    private ToolCriteria buildCriteria(
            String query,
            String location,
            String category,
            BigDecimal minRating,
            boolean openNow,
            String sortBy,
            int limit,
            BigDecimal latitude,
            BigDecimal longitude
    ) {
        String normalizedQuery = normalize(query);
        String normalizedLocation = normalize(location);
        boolean proximityRequested = hasProximityIntent(normalizedQuery)
                || hasProximityIntent(normalizedLocation);
        String cleanLocation = proximityRequested ? "" : location;
        String cleanQuery = cleanQuery(query);
        String cleanSortBy = sortBy == null || sortBy.isBlank() ? "relevance" : sortBy;
        if (proximityRequested && latitude != null && longitude != null) {
            cleanSortBy = "distance";
        }
        return new ToolCriteria(cleanQuery, cleanLocation, category, minRating, openNow, cleanSortBy, limit,
                latitude, longitude);
    }

    private List<CuaHangVo> searchRestaurants(
            ToolCriteria criteria,
            List<CuaHangVo> restaurants,
            List<MenuItem> menuItems
    ) {
        int limit = Math.max(1, Math.min(criteria.limit(), 5));
        return restaurants.stream()
                .map(restaurant -> scoreRestaurant(criteria, restaurant, menuItems))
                .filter(ScoredRestaurant::matched)
                .sorted(comparator(criteria))
                .limit(limit)
                .map(ScoredRestaurant::restaurant)
                .toList();
    }

    private ScoredRestaurant scoreRestaurant(ToolCriteria criteria, CuaHangVo restaurant, List<MenuItem> menuItems) {
        int score = 0;
        String query = normalize(criteria.query());
        String location = normalize(criteria.location());
        String restaurantText = restaurantText(restaurant);
        String menuText = menuText(restaurant.getId(), menuItems);

        if (!criteria.category().isBlank() && !criteria.category().equalsIgnoreCase(restaurant.getLoaiCuaHang())) {
            return ScoredRestaurant.unmatched(restaurant);
        }
        if (!location.isBlank()) {
            if (!containsAllTerms(normalize(restaurant.getDiaChi()), location)) {
                return ScoredRestaurant.unmatched(restaurant);
            }
            score += 35;
        }
        if (!query.isBlank()) {
            int queryScore = scoreText(restaurantText + " " + menuText, query);
            if (queryScore == 0) {
                return ScoredRestaurant.unmatched(restaurant);
            }
            score += queryScore;
        }

        BigDecimal rating = restaurant.getDiemDanhGiaTrungBinh() == null
                ? BigDecimal.ZERO
                : restaurant.getDiemDanhGiaTrungBinh();
        if (criteria.minRating() != null && rating.compareTo(criteria.minRating()) < 0) {
            return ScoredRestaurant.unmatched(restaurant);
        }
        score += rating.multiply(BigDecimal.TEN).intValue();

        if (criteria.openNow() && !isRestaurantOpen(restaurant)) {
            return ScoredRestaurant.unmatched(restaurant);
        }
        if (criteria.openNow()) {
            score += 12;
        }

        Double distance = distanceInMeters(criteria.latitude(), criteria.longitude(), restaurant);
        if (distance != null) {
            score += Math.max(0, 25 - (int) Math.min(distance / 300, 25));
        }
        return new ScoredRestaurant(restaurant, true, score, rating, distance);
    }

    private Comparator<ScoredRestaurant> comparator(ToolCriteria criteria) {
        if ("distance".equalsIgnoreCase(criteria.sortBy()) && criteria.latitude() != null && criteria.longitude() != null) {
            return Comparator.comparing(ScoredRestaurant::distance, Comparator.nullsLast(Comparator.naturalOrder()))
                    .thenComparing(ScoredRestaurant::score, Comparator.reverseOrder())
                    .thenComparing(ScoredRestaurant::rating, Comparator.reverseOrder());
        }
        if ("rating".equalsIgnoreCase(criteria.sortBy())) {
            return Comparator.comparing(ScoredRestaurant::rating, Comparator.reverseOrder())
                    .thenComparing(ScoredRestaurant::score, Comparator.reverseOrder());
        }
        return Comparator.comparing(ScoredRestaurant::score, Comparator.reverseOrder())
                .thenComparing(ScoredRestaurant::rating, Comparator.reverseOrder());
    }

    private int scoreText(String text, String query) {
        String normalizedText = normalize(text);
        List<String> words = List.of(query.split(" ")).stream()
                .filter(word -> word.length() >= 2)
                .toList();
        int score = 0;
        if (normalizedText.contains(query)) {
            score += 45;
        }
        for (String word : words) {
            if (normalizedText.contains(word)) {
                score += 12;
            }
        }
        return score;
    }

    private boolean containsAllTerms(String text, String terms) {
        List<String> words = List.of(terms.split(" ")).stream()
                .filter(word -> word.length() >= 2)
                .toList();
        return !words.isEmpty() && words.stream().allMatch(text::contains);
    }

    private boolean hasProximityIntent(String normalizedText) {
        if (normalizedText.isBlank()) {
            return false;
        }
        return normalizedText.contains("gan toi")
                || normalizedText.contains("gan minh")
                || normalizedText.contains("gan day")
                || normalizedText.contains("quanh day")
                || normalizedText.contains("xung quanh")
                || normalizedText.contains("gan nhat")
                || normalizedText.equals("gan")
                || normalizedText.equals("gan toi");
    }

    private String cleanQuery(String query) {
        String normalizedQuery = normalize(query);
        if (normalizedQuery.isBlank()) {
            return "";
        }
        List<String> words = List.of(normalizedQuery.split(" ")).stream()
                .filter(word -> word.length() >= 2)
                .filter(word -> !isQueryStopWord(word))
                .toList();
        return String.join(" ", words);
    }

    private boolean isQueryStopWord(String word) {
        return List.of(
                "toi", "minh", "ban", "muon", "can", "tim", "goi", "y", "cho", "co", "khong",
                "quan", "an", "uong", "gan", "day", "quanh", "xung", "nhat", "dia", "diem",
                "khu", "vuc", "o", "tai", "vi", "tri", "nao", "mot", "may"
        ).contains(word);
    }

    private boolean isNoResultAnswer(String answer) {
        String normalizedAnswer = normalize(answer);
        return normalizedAnswer.contains("khong tim thay")
                || normalizedAnswer.contains("chua tim thay")
                || normalizedAnswer.contains("khong co quan")
                || normalizedAnswer.contains("khong thay quan")
                || normalizedAnswer.contains("rat tiec");
    }

    private boolean isRestaurantOpen(CuaHangVo restaurant) {
        try {
            if (restaurant.getGioMoCua() == null || restaurant.getGioDongCua() == null) {
                return false;
            }
            LocalTime now = LocalTime.now();
            LocalTime open = LocalTime.parse(restaurant.getGioMoCua());
            LocalTime close = LocalTime.parse(restaurant.getGioDongCua());
            if (close.isBefore(open)) {
                return !now.isBefore(open) || !now.isAfter(close);
            }
            return !now.isBefore(open) && !now.isAfter(close);
        } catch (RuntimeException ex) {
            return false;
        }
    }

    private String serializeToolResult(List<CuaHangVo> matches, List<MenuItem> menuItems) {
        try {
            List<Map<String, Object>> items = matches.stream()
                    .map(restaurant -> {
                        Map<String, Object> item = new LinkedHashMap<>();
                        item.put("id", restaurant.getId().toString());
                        item.put("name", nullToEmpty(restaurant.getTenQuanAn()));
                        item.put("address", nullToEmpty(restaurant.getDiaChi()));
                        item.put("category", nullToEmpty(restaurant.getLoaiCuaHang()));
                        item.put("businessType", nullToEmpty(restaurant.getLoaiKinhDoanh()));
                        item.put("rating", restaurant.getDiemDanhGiaTrungBinh() == null
                                ? BigDecimal.ZERO
                                : restaurant.getDiemDanhGiaTrungBinh());
                        item.put("reviewCount", restaurant.getSoLuongDanhGia());
                        item.put("openTime", nullToEmpty(restaurant.getGioMoCua()));
                        item.put("closeTime", nullToEmpty(restaurant.getGioDongCua()));
                        item.put("description", nullToEmpty(restaurant.getMoTa()));
                        item.put("menu", menuNames(restaurant.getId(), menuItems));
                        return item;
                    })
                    .toList();
            return objectMapper.writeValueAsString(Map.of("restaurants", items));
        } catch (JsonProcessingException ex) {
            return "{\"restaurants\":[]}";
        }
    }

    private List<String> menuNames(UUID restaurantId, List<MenuItem> menuItems) {
        return menuItems.stream()
                .filter(item -> item.getRestaurant() != null && restaurantId.equals(item.getRestaurant().getId()))
                .limit(8)
                .map(MenuItem::getName)
                .filter(name -> name != null && !name.isBlank())
                .toList();
    }

    private ChatSession createSession(String question) {
        ChatSession session = new ChatSession();
        SecurityUtils.getCurrentUser()
                .flatMap(user -> userRepository.findById(user.getId()))
                .ifPresent(session::setUser);
        session.setTitle(buildTitle(question));
        return chatSessionRepository.save(session);
    }

    private void saveMessage(ChatSession session, String role, String content, String jsonData) {
        ChatMessage message = new ChatMessage();
        message.setSession(session);
        message.setRole(role);
        message.setContent(content);
        message.setJsonData(jsonData);
        message.setTokenCount(estimateTokenCount(content));
        chatMessageRepository.save(message);
    }

    private ChatHistoryResponse mapToHistory(ChatSession session) {
        String question = chatMessageRepository.findFirstBySessionIdAndRoleOrderByCreatedAtAsc(session.getId(), "user")
                .map(ChatMessage::getContent)
                .orElse(session.getTitle());
        String answer = chatMessageRepository.findFirstBySessionIdAndRoleOrderByCreatedAtAsc(session.getId(), "assistant")
                .map(ChatMessage::getContent)
                .orElse("");
        return ChatHistoryResponse.builder()
                .id(session.getId())
                .question(question)
                .answer(answer)
                .createdAt(session.getCreatedAt())
                .build();
    }

    private String buildJsonData(List<CuaHangVo> matches) {
        try {
            List<UUID> restaurantIds = matches.stream()
                    .map(CuaHangVo::getId)
                    .toList();
            return objectMapper.writeValueAsString(Map.of("restaurantIds", restaurantIds));
        } catch (JsonProcessingException ex) {
            return "{}";
        }
    }

    private String buildTitle(String question) {
        String trimmed = question == null ? "Phien chat moi" : question.trim();
        if (trimmed.length() <= 80) {
            return trimmed;
        }
        return trimmed.substring(0, 80);
    }

    private int estimateTokenCount(String text) {
        if (text == null || text.isBlank()) {
            return 0;
        }
        return Math.max(1, text.length() / 4);
    }

    private String buildFallbackAnswer(List<CuaHangVo> matches) {
        if (matches.isEmpty()) {
            return "Minh chua tim thay quan phu hop. Ban thu noi ro hon ve mon an, khu vuc hoac loai quan nhe.";
        }
        String names = matches.stream()
                .map(CuaHangVo::getTenQuanAn)
                .limit(3)
                .collect(Collectors.joining(", "));
        return "Minh goi y cho ban mot so quan phu hop: " + names + ".";
    }

    private String restaurantText(CuaHangVo restaurant) {
        return normalize("%s %s %s %s %s".formatted(
                nullToEmpty(restaurant.getTenQuanAn()),
                nullToEmpty(restaurant.getDiaChi()),
                nullToEmpty(restaurant.getLoaiCuaHang()),
                nullToEmpty(restaurant.getLoaiKinhDoanh()),
                nullToEmpty(restaurant.getMoTa())));
    }

    private String menuText(UUID restaurantId, List<MenuItem> menuItems) {
        return normalize(menuItems.stream()
                .filter(item -> item.getRestaurant() != null && restaurantId.equals(item.getRestaurant().getId()))
                .map(item -> "%s %s %s".formatted(
                        nullToEmpty(item.getName()),
                        nullToEmpty(item.getFlavor()),
                        nullToEmpty(item.getDescription())))
                .collect(Collectors.joining(" ")));
    }

    private Double distanceInMeters(BigDecimal latitude, BigDecimal longitude, CuaHangVo restaurant) {
        if (latitude == null || longitude == null || restaurant.getLatitude() == null || restaurant.getLongitude() == null) {
            return null;
        }
        double lat1 = Math.toRadians(latitude.doubleValue());
        double lat2 = Math.toRadians(restaurant.getLatitude().doubleValue());
        double deltaLat = Math.toRadians(restaurant.getLatitude().doubleValue() - latitude.doubleValue());
        double deltaLon = Math.toRadians(restaurant.getLongitude().doubleValue() - longitude.doubleValue());
        double a = Math.sin(deltaLat / 2) * Math.sin(deltaLat / 2)
                + Math.cos(lat1) * Math.cos(lat2) * Math.sin(deltaLon / 2) * Math.sin(deltaLon / 2);
        return 6371000D * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    }

    private String normalize(String value) {
        if (value == null) {
            return "";
        }
        String withoutDiacritics = DIACRITICS_PATTERN.matcher(
                        Normalizer.normalize(value, Normalizer.Form.NFD))
                .replaceAll("")
                .replace('đ', 'd')
                .replace('Đ', 'D');
        return withoutDiacritics.toLowerCase(Locale.ROOT)
                .replace('_', ' ')
                .replace('-', ' ')
                .replaceAll("[^a-z0-9\\s]", " ")
                .replaceAll("\\s+", " ")
                .trim();
    }

    private String text(JsonNode node, String fieldName) {
        JsonNode value = node == null ? null : node.get(fieldName);
        return value == null || value.isNull() ? "" : value.asText().trim();
    }

    private BigDecimal decimal(JsonNode node, String fieldName) {
        JsonNode value = node == null ? null : node.get(fieldName);
        if (value == null || value.isNull() || !value.isNumber()) {
            return null;
        }
        return value.decimalValue();
    }

    private int intValue(JsonNode node, String fieldName, int defaultValue) {
        JsonNode value = node == null ? null : node.get(fieldName);
        return value == null || !value.canConvertToInt() ? defaultValue : value.asInt(defaultValue);
    }

    private String nullToEmpty(Object value) {
        return value == null ? "" : value.toString();
    }

    private record ToolCriteria(
            String query,
            String location,
            String category,
            BigDecimal minRating,
            boolean openNow,
            String sortBy,
            int limit,
            BigDecimal latitude,
            BigDecimal longitude
    ) {
    }

    private record ScoredRestaurant(
            CuaHangVo restaurant,
            boolean matched,
            int score,
            BigDecimal rating,
            Double distance
    ) {
        static ScoredRestaurant unmatched(CuaHangVo restaurant) {
            return new ScoredRestaurant(restaurant, false, 0, BigDecimal.ZERO, null);
        }
    }
}
