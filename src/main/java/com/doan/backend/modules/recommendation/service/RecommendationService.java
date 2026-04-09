package com.doan.backend.modules.recommendation.service;

import com.doan.backend.modules.recommendation.dto.request.RecommendationRequest;
import com.doan.backend.modules.recommendation.dto.response.RecommendationResponse;

public interface RecommendationService {
    RecommendationResponse suggest(RecommendationRequest request);

    RecommendationResponse popular();
}
