package com.doan.backend.modules.review.service;

import com.doan.backend.modules.review.dto.request.ReviewCreateByRestaurantRequest;
import com.doan.backend.modules.review.dto.request.ReviewCreateRequest;
import com.doan.backend.modules.review.vo.RestaurantReviewListVo;
import com.doan.backend.modules.review.vo.ReviewSummaryVo;
import com.doan.backend.modules.review.vo.ReviewVo;
import java.util.UUID;

public interface ReviewService {

    ReviewVo create(ReviewCreateRequest request);

    ReviewVo create(UUID restaurantId, ReviewCreateByRestaurantRequest request);

    RestaurantReviewListVo findByRestaurant(UUID restaurantId);

    RestaurantReviewListVo findByRestaurant(UUID restaurantId, int page, int size);

    ReviewSummaryVo getSummary(UUID restaurantId);

    void delete(UUID id);
}
