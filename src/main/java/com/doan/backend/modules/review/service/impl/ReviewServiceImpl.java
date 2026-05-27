package com.doan.backend.modules.review.service.impl;

import com.doan.backend.common.exception.ResourceNotFoundException;
import com.doan.backend.common.exception.UnauthorizedException;
import com.doan.backend.modules.restaurant.entity.Restaurant;
import com.doan.backend.modules.restaurant.repository.RestaurantRepository;
import com.doan.backend.modules.review.dto.request.ReviewCreateByRestaurantRequest;
import com.doan.backend.modules.review.dto.request.ReviewCreateRequest;
import com.doan.backend.modules.review.entity.Review;
import com.doan.backend.modules.review.repository.ReviewRepository;
import com.doan.backend.modules.review.service.ReviewService;
import com.doan.backend.modules.review.vo.RestaurantReviewListVo;
import com.doan.backend.modules.review.vo.ReviewSummaryVo;
import com.doan.backend.modules.review.vo.ReviewVo;
import com.doan.backend.modules.user.entity.User;
import com.doan.backend.modules.user.repository.UserRepository;
import com.doan.backend.security.SecurityUtils;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ReviewServiceImpl implements ReviewService {

    private final ReviewRepository reviewRepository;
    private final RestaurantRepository restaurantRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public ReviewVo create(ReviewCreateRequest request) {
        return createReview(request.getIdCuaHang(), request.getDanhGia(), request.getDiemDanhGia());
    }

    @Override
    @Transactional
    public ReviewVo create(UUID restaurantId, ReviewCreateByRestaurantRequest request) {
        return createReview(restaurantId, request.getDanhGia(), request.getDiemDanhGia());
    }

    @Override
    @Transactional(readOnly = true)
    public RestaurantReviewListVo findByRestaurant(UUID restaurantId) {
        return findByRestaurant(restaurantId, 0, Integer.MAX_VALUE);
    }

    @Override
    @Transactional(readOnly = true)
    public RestaurantReviewListVo findByRestaurant(UUID restaurantId, int page, int size) {
        ensureRestaurantExists(restaurantId);
        ReviewSummaryVo summary = getSummary(restaurantId);
        int normalizedPage = Math.max(page, 0);
        int normalizedSize = Math.max(1, Math.min(size, 50));
        Pageable pageable = PageRequest.of(normalizedPage, normalizedSize);
        Page<Review> reviewPage = reviewRepository.findPageByRestaurantIdOrderByCreatedAtDesc(restaurantId, pageable);
        List<ReviewVo> reviews = reviewPage.getContent().stream()
                .map(this::mapToResponse)
                .toList();
        return RestaurantReviewListVo.builder()
                .idCuaHang(restaurantId)
                .diemDanhGiaTrungBinh(summary.getDiemDanhGiaTrungBinh())
                .soLuongDanhGia(summary.getSoLuongDanhGia())
                .page(reviewPage.getNumber())
                .size(reviewPage.getSize())
                .totalElements(reviewPage.getTotalElements())
                .totalPages(reviewPage.getTotalPages())
                .danhSachDanhGia(reviews)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public ReviewSummaryVo getSummary(UUID restaurantId) {
        List<Object[]> summaries = reviewRepository.getRatingSummary(restaurantId);
        Object[] summary = summaries.isEmpty() ? new Object[]{0D, 0L} : summaries.get(0);
        Double average = summary[0] instanceof Number number ? number.doubleValue() : 0D;
        Long total = summary[1] instanceof Number number ? number.longValue() : 0L;
        return ReviewSummaryVo.builder()
                .diemDanhGiaTrungBinh(BigDecimal.valueOf(average).setScale(1, RoundingMode.HALF_UP))
                .soLuongDanhGia(total)
                .build();
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        UUID userId = getCurrentUserId();
        Review review = reviewRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đánh giá"));
        UUID restaurantId = review.getRestaurant() == null ? null : review.getRestaurant().getId();
        reviewRepository.delete(review);
        if (restaurantId != null) {
            restaurantRepository.refreshRating(restaurantId);
        }
    }

    private ReviewVo createReview(UUID restaurantId, String content, Integer rating) {
        UUID userId = getCurrentUserId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy tài khoản"));
        Restaurant restaurant = restaurantRepository.findActiveById(restaurantId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy quán ăn"));

        Review review = new Review();
        review.setUser(user);
        review.setRestaurant(restaurant);
        review.setContent(content);
        review.setRating(rating);
        Review savedReview = reviewRepository.save(review);
        restaurantRepository.refreshRating(restaurantId);
        return mapToResponse(savedReview);
    }

    private void ensureRestaurantExists(UUID restaurantId) {
        if (!restaurantRepository.existsActiveById(restaurantId)) {
            throw new ResourceNotFoundException("Không tìm thấy quán ăn");
        }
    }

    private UUID getCurrentUserId() {
        return SecurityUtils.getCurrentUser()
                .map(user -> user.getId())
                .orElseThrow(() -> new UnauthorizedException("Vui lòng đăng nhập để đánh giá quán ăn"));
    }

    private ReviewVo mapToResponse(Review review) {
        User user = review.getUser();
        Restaurant restaurant = review.getRestaurant();
        return ReviewVo.builder()
                .id(review.getId())
                .idTaiKhoan(user == null ? null : user.getId())
                .idCuaHang(restaurant == null ? null : restaurant.getId())
                .tenTaiKhoan(user == null ? null : user.getUsername())
                .tenQuanAn(restaurant == null ? null : restaurant.getName())
                .danhGia(review.getContent())
                .diemDanhGia(review.getRating())
                .ngayTao(review.getCreatedAt())
                .build();
    }
}
