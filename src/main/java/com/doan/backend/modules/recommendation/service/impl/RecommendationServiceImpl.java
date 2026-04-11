package com.doan.backend.modules.recommendation.service.impl;

import com.doan.backend.modules.recommendation.dto.request.RecommendationRequest;
import com.doan.backend.modules.recommendation.dto.response.RecommendationResponse;
import com.doan.backend.modules.recommendation.service.RecommendationService;
import com.doan.backend.modules.restaurant.service.RestaurantService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RecommendationServiceImpl implements RecommendationService {

    private final RestaurantService restaurantService;

    @Override
    public RecommendationResponse suggest(RecommendationRequest request) {
        var restaurants = restaurantService.search(request.getKeyword(), 0, 5).getItems();
        return RecommendationResponse.builder()
                .message(restaurants.isEmpty()
                        ? "Chưa có gợi ý phù hợp từ từ khóa hiện tại."
                        : "Đây là danh sách quán ăn phù hợp với yêu cầu của bạn.")
                .cuaHangs(restaurants)
                .build();
    }

    @Override
    public RecommendationResponse popular() {
        var restaurants = restaurantService.search(null, 0, 5).getItems();
        return RecommendationResponse.builder()
                .message("Danh sách quán ăn nổi bật để frontend có thể hiển thị nhanh.")
                .cuaHangs(restaurants)
                .build();
    }
}
