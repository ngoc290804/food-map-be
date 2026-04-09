package com.doan.backend.modules.recommendation.controller;

import com.doan.backend.common.dto.ApiResponse;
import com.doan.backend.modules.recommendation.dto.request.RecommendationRequest;
import com.doan.backend.modules.recommendation.dto.response.RecommendationResponse;
import com.doan.backend.modules.recommendation.service.RecommendationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/recommendations")
@RequiredArgsConstructor
public class RecommendationController {

    private final RecommendationService recommendationService;

    @PostMapping("/suggest")
    public ApiResponse<RecommendationResponse> suggest(@RequestBody RecommendationRequest request) {
        return ApiResponse.success(recommendationService.suggest(request));
    }

    @GetMapping("/popular")
    public ApiResponse<RecommendationResponse> popular() {
        return ApiResponse.success(recommendationService.popular());
    }
}
