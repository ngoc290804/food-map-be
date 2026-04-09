package com.doan.backend.modules.recommendation.dto.response;

import com.doan.backend.modules.restaurant.dto.response.RestaurantResponse;
import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class RecommendationResponse {
    private final String message;
    private final List<RestaurantResponse> restaurants;
}
