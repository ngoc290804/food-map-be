package com.doan.backend.modules.review.controller;

import com.doan.backend.common.dto.ApiResponse;
import com.doan.backend.modules.review.dto.request.ReviewCreateByRestaurantRequest;
import com.doan.backend.modules.review.dto.request.ReviewCreateRequest;
import com.doan.backend.modules.review.service.ReviewService;
import com.doan.backend.modules.review.vo.RestaurantReviewListVo;
import com.doan.backend.modules.review.vo.ReviewVo;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    @PostMapping
    public ApiResponse<ReviewVo> create(@Valid @RequestBody ReviewCreateRequest request) {
        return ApiResponse.success("Thêm đánh giá thành công", reviewService.create(request));
    }

    @PostMapping("/restaurants/{restaurantId}")
    public ApiResponse<ReviewVo> createByRestaurant(
            @PathVariable UUID restaurantId,
            @Valid @RequestBody ReviewCreateByRestaurantRequest request
    ) {
        return ApiResponse.success("Thêm đánh giá thành công", reviewService.create(restaurantId, request));
    }

    @GetMapping("/restaurants/{restaurantId}")
    public ApiResponse<RestaurantReviewListVo> findByRestaurant(
            @PathVariable UUID restaurantId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ApiResponse.success(reviewService.findByRestaurant(restaurantId, page, size));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<String> delete(@PathVariable UUID id) {
        reviewService.delete(id);
        return ApiResponse.success("Xóa đánh giá thành công", "Xóa đánh giá thành công");
    }
}
