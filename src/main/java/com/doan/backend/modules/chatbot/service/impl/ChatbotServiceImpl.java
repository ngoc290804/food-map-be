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
import com.doan.backend.modules.menu.entity.MenuItem;
import com.doan.backend.modules.menu.repository.MenuItemRepository;
import com.doan.backend.modules.restaurant.service.RestaurantService;
import com.doan.backend.modules.restaurant.vo.CuaHangVo;
import com.doan.backend.modules.user.entity.User;
import com.doan.backend.modules.user.repository.UserRepository;
import com.doan.backend.security.SecurityUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.text.Normalizer;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ChatbotServiceImpl implements ChatbotService {

    private static final Pattern DIACRITICS_PATTERN = Pattern.compile("\\p{M}+");
    private static final Set<String> STOP_WORDS = Set.of(
            "toi", "minh", "muon", "an", "uong", "quan", "quan an", "tim", "goi", "y", "cho", "gan", "o", "khu",
            "vuc", "nao", "co", "khong", "thich", "can", "mot", "may", "dia", "diem", "danh", "gia", "cao",
            "tot", "sao", "nhat", "tren", "tu"
    );

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
        var allRestaurants = restaurantService.findActiveForChatbot(
                Math.max(1, openAiProperties.getMaxRestaurants()));
        List<MenuItem> menuItems = menuItemRepository.findActiveRestaurantMenuItems();
        String restaurantContext = buildRestaurantContext(allRestaurants, menuItems);
        String openAiAnswer = openAiChatClient.generateAnswer(request.getQuestion(), restaurantContext);
        List<CuaHangVo> suggestedRestaurants = selectFallbackRestaurants(request, allRestaurants, menuItems);
        String answer = openAiAnswer == null || openAiAnswer.isBlank()
                ? buildFallbackAnswer(suggestedRestaurants)
                : openAiAnswer;
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
        String trimmed = question == null ? "Phiên chat mới" : question.trim();
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

    private String buildRestaurantContext(List<CuaHangVo> restaurants, List<MenuItem> menuItems) {
        if (restaurants.isEmpty()) {
            return "Không tìm thấy quán phù hợp trong hệ thống.";
        }
        return restaurants.stream()
                .map(restaurant -> "- Tên: %s | Địa chỉ: %s | Loại cửa hàng: %s | Loại kinh doanh: %s | Điểm: %s | Mô tả: %s | Menu: %s"
                        .formatted(
                                nullToEmpty(restaurant.getTenQuanAn()),
                                nullToEmpty(restaurant.getDiaChi()),
                                nullToEmpty(restaurant.getLoaiCuaHang()),
                                nullToEmpty(restaurant.getLoaiKinhDoanh()),
                                restaurant.getDiemDanhGiaTrungBinh() == null
                                        ? "0"
                                        : restaurant.getDiemDanhGiaTrungBinh(),
                                nullToEmpty(restaurant.getMoTa()),
                                buildMenuSummary(restaurant.getId(), menuItems)))
                .collect(Collectors.joining("\n"));
    }

    private String buildFallbackAnswer(List<CuaHangVo> matches) {
        if (matches.isEmpty()) {
            return "Mình chưa tìm thấy quán phù hợp, bạn thử mô tả rõ hơn về món, vị hoặc khu vực.";
        }
        String names = matches.stream()
                .map(CuaHangVo::getTenQuanAn)
                .limit(3)
                .collect(Collectors.joining(", "));
        return "Mình gợi ý cho bạn một số quán phù hợp: " + names + ".";
    }

    private List<CuaHangVo> selectFallbackRestaurants(
            ChatbotAskRequest request,
            List<CuaHangVo> restaurants,
            List<MenuItem> menuItems
    ) {
        SearchCriteria criteria = buildSearchCriteria(request);
        return restaurants.stream()
                .map(restaurant -> scoreRestaurant(criteria, restaurant, menuItems))
                .filter(ScoredRestaurant::matched)
                .sorted(scoredComparator(criteria))
                .limit(5)
                .map(ScoredRestaurant::restaurant)
                .toList();
    }

    private SearchCriteria buildSearchCriteria(ChatbotAskRequest request) {
        String normalizedQuestion = normalize(request.getQuestion());
        String intent = detectFoodIntent(normalizedQuestion);
        boolean locationAsked = asksLocation(normalizedQuestion);
        boolean nearestAsked = asksNearest(normalizedQuestion);
        List<String> words = meaningfulWords(normalizedQuestion);
        List<String> locationWords = locationAsked
                ? extractLocationWords(normalizedQuestion, intent)
                : List.of();
        BigDecimal minRating = extractMinRating(normalizedQuestion);
        return new SearchCriteria(
                normalizedQuestion,
                intent,
                words,
                locationWords,
                minRating,
                nearestAsked,
                request.getLatitude(),
                request.getLongitude());
    }

    private ScoredRestaurant scoreRestaurant(SearchCriteria criteria, CuaHangVo restaurant, List<MenuItem> menuItems) {
        int score = 0;
        if (!criteria.foodIntent().isBlank()) {
            if (!restaurantMatchesIntent(restaurant, criteria.foodIntent(), menuItems)) {
                return ScoredRestaurant.unmatched(restaurant);
            }
            score += 40;
        }

        if (!criteria.locationWords().isEmpty()) {
            if (restaurantMatchesLocation(restaurant, criteria.locationWords())) {
                score += 30;
            } else if (restaurantMatchesAnyLocationWord(restaurant, criteria.locationWords())) {
                score += 15;
            } else {
                return ScoredRestaurant.unmatched(restaurant);
            }
        }

        BigDecimal rating = restaurant.getDiemDanhGiaTrungBinh() == null
                ? BigDecimal.ZERO
                : restaurant.getDiemDanhGiaTrungBinh();
        if (criteria.minRating() != null && rating.compareTo(criteria.minRating()) < 0) {
            return ScoredRestaurant.unmatched(restaurant);
        }
        score += rating.multiply(BigDecimal.TEN).intValue();

        if (criteria.foodIntent().isBlank() && criteria.locationWords().isEmpty()) {
            if (!matchesQuestion(criteria.normalizedQuestion(), restaurant, menuItems, false, criteria.meaningfulWords())) {
                return ScoredRestaurant.unmatched(restaurant);
            }
            score += 10;
        }

        Double distance = distanceInMeters(criteria.latitude(), criteria.longitude(), restaurant);
        if (criteria.nearestRequested() && distance != null) {
            score += Math.max(0, 30 - (int) Math.min(distance / 250, 30));
        }
        return new ScoredRestaurant(restaurant, true, score, rating, distance);
    }

    private Comparator<ScoredRestaurant> scoredComparator(SearchCriteria criteria) {
        Comparator<ScoredRestaurant> comparator = Comparator.comparing(ScoredRestaurant::score).reversed()
                .thenComparing(ScoredRestaurant::rating, Comparator.reverseOrder());
        if (criteria.nearestRequested() && criteria.hasCoordinates()) {
            comparator = Comparator.comparing(
                            ScoredRestaurant::distance,
                            Comparator.nullsLast(Comparator.naturalOrder()))
                    .thenComparing(comparator);
        }
        return comparator;
    }

    private String detectFoodIntent(String normalizedQuestion) {
        List<String> intents = List.of(
                "mi",
                "my",
                "lau",
                "pho",
                "bun",
                "com",
                "ga",
                "tra sua",
                "trasua",
                "cafe",
                "coffee",
                "pizza",
                "banh mi",
                "banhmi"
        );
        return intents.stream()
                .filter(normalizedQuestion::contains)
                .findFirst()
                .map(intent -> switch (intent) {
                    case "trasua" -> "tra sua";
                    case "coffee" -> "cafe";
                    case "banhmi" -> "banh mi";
                    default -> intent;
                })
                .orElse("");
    }

    private boolean matchesQuestion(
            String normalizedQuestion,
            CuaHangVo restaurant,
            List<MenuItem> menuItems,
            boolean asksLocation,
            List<String> meaningfulWords
    ) {
        if (normalizedQuestion.isBlank()) {
            return false;
        }
        if (asksLocation) {
            return restaurantMatchesLocation(restaurant, meaningfulWords);
        }
        String restaurantText = normalize("%s %s %s %s %s".formatted(
                nullToEmpty(restaurant.getTenQuanAn()),
                nullToEmpty(restaurant.getDiaChi()),
                nullToEmpty(restaurant.getLoaiCuaHang()),
                nullToEmpty(restaurant.getLoaiKinhDoanh()),
                nullToEmpty(restaurant.getMoTa())));
        String menuText = menuText(restaurant.getId(), menuItems);
        return meaningfulWords.stream()
                .anyMatch(word -> containsTerm(restaurantText, word) || containsTerm(menuText, word));
    }

    private boolean restaurantMatchesIntent(CuaHangVo restaurant, String intent, List<MenuItem> menuItems) {
        String restaurantText = normalize("%s %s %s".formatted(
                nullToEmpty(restaurant.getTenQuanAn()),
                nullToEmpty(restaurant.getLoaiKinhDoanh()),
                nullToEmpty(restaurant.getMoTa())));
        return containsTerm(restaurantText, intent) || containsTerm(menuText(restaurant.getId(), menuItems), intent);
    }

    private boolean restaurantMatchesLocation(CuaHangVo restaurant, List<String> words) {
        String address = normalize(restaurant.getDiaChi());
        if (words.isEmpty()) {
            return false;
        }
        String phrase = String.join(" ", words);
        return address.contains(phrase) || words.stream().allMatch(word -> containsTerm(address, word));
    }

    private boolean restaurantMatchesAnyLocationWord(CuaHangVo restaurant, List<String> words) {
        String address = normalize(restaurant.getDiaChi());
        return words.stream()
                .filter(word -> word.length() >= 4)
                .anyMatch(word -> containsTerm(address, word));
    }

    private boolean containsTerm(String text, String term) {
        if (text == null || text.isBlank() || term == null || term.isBlank()) {
            return false;
        }
        if (term.contains(" ")) {
            return text.contains(term);
        }
        return Pattern.compile("\\b" + Pattern.quote(term) + "\\b").matcher(text).find();
    }

    private boolean asksLocation(String normalizedQuestion) {
        return normalizedQuestion.contains(" o ")
                || normalizedQuestion.startsWith("o ")
                || normalizedQuestion.contains("gan ")
                || normalizedQuestion.contains("dia chi")
                || normalizedQuestion.contains("vi tri")
                || normalizedQuestion.contains("khu vuc");
    }

    private boolean asksNearest(String normalizedQuestion) {
        return normalizedQuestion.contains("gan nhat") || normalizedQuestion.contains("gan toi");
    }

    private List<String> extractLocationWords(String normalizedQuestion, String foodIntent) {
        String locationText = "";
        List<String> markers = List.of(" o ", " tai ", " khu vuc ", " dia chi ", " vi tri ");
        for (String marker : markers) {
            int index = normalizedQuestion.indexOf(marker);
            if (index >= 0) {
                locationText = normalizedQuestion.substring(index + marker.length());
                break;
            }
        }
        if (locationText.isBlank() && normalizedQuestion.startsWith("o ")) {
            locationText = normalizedQuestion.substring(2);
        }
        List<String> words = meaningfulWords(locationText.isBlank() ? normalizedQuestion : locationText);
        if (foodIntent.isBlank()) {
            return words;
        }
        Set<String> foodWords = Arrays.stream(foodIntent.split("\\s+")).collect(Collectors.toSet());
        return words.stream()
                .filter(word -> !foodWords.contains(word))
                .toList();
    }

    private BigDecimal extractMinRating(String normalizedQuestion) {
        java.util.regex.Matcher starMatcher = Pattern.compile("\\b([1-5])\\s*sao\\b").matcher(normalizedQuestion);
        if (starMatcher.find()) {
            return new BigDecimal(starMatcher.group(1));
        }
        java.util.regex.Matcher rangeMatcher = Pattern.compile("\\b(?:tren|tu|>=)\\s*([1-5])\\b").matcher(normalizedQuestion);
        if (rangeMatcher.find()) {
            return new BigDecimal(rangeMatcher.group(1));
        }
        if (normalizedQuestion.contains("danh gia cao")
                || normalizedQuestion.contains("diem cao")
                || normalizedQuestion.contains("tot")) {
            return new BigDecimal("4.0");
        }
        return null;
    }

    private List<String> meaningfulWords(String normalizedQuestion) {
        return Arrays.stream(normalizedQuestion.split("\\s+"))
                .filter(word -> word.length() >= 2)
                .filter(word -> !STOP_WORDS.contains(word))
                .toList();
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

    private String buildMenuSummary(UUID restaurantId, List<MenuItem> menuItems) {
        String summary = menuItems.stream()
                .filter(item -> item.getRestaurant() != null && restaurantId.equals(item.getRestaurant().getId()))
                .limit(8)
                .map(item -> "%s%s".formatted(
                        nullToEmpty(item.getName()),
                        item.getFlavor() == null || item.getFlavor().isBlank()
                                ? ""
                                : " (" + item.getFlavor() + ")"))
                .collect(Collectors.joining(", "));
        return summary.isBlank() ? "Chưa có menu" : summary;
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

    private String normalize(String value) {
        if (value == null) {
            return "";
        }
        String withoutDiacritics = DIACRITICS_PATTERN.matcher(
                        Normalizer.normalize(value, Normalizer.Form.NFD))
                .replaceAll("")
                .replace('đ', 'd')
                .replace('Đ', 'D');
        return withoutDiacritics.toLowerCase()
                .replace('_', ' ')
                .replace('-', ' ')
                .replaceAll("[^a-z0-9\\s]", " ")
                .replaceAll("\\s+", " ")
                .trim();
    }

    private String nullToEmpty(Object value) {
        return value == null ? "" : value.toString();
    }

    private record SearchCriteria(
            String normalizedQuestion,
            String foodIntent,
            List<String> meaningfulWords,
            List<String> locationWords,
            BigDecimal minRating,
            boolean nearestRequested,
            BigDecimal latitude,
            BigDecimal longitude
    ) {
        boolean hasCoordinates() {
            return latitude != null && longitude != null;
        }
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
