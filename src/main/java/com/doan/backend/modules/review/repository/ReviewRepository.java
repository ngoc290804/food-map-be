package com.doan.backend.modules.review.repository;

import com.doan.backend.modules.review.entity.Review;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ReviewRepository extends JpaRepository<Review, UUID> {

    List<Review> findByRestaurantIdOrderByCreatedAtDesc(UUID restaurantId);

    Page<Review> findPageByRestaurantIdOrderByCreatedAtDesc(UUID restaurantId, Pageable pageable);

    Optional<Review> findByIdAndUserId(UUID id, UUID userId);

    @Query("select coalesce(avg(r.rating), 0), count(r) from Review r where r.restaurant.id = :restaurantId")
    List<Object[]> getRatingSummary(@Param("restaurantId") UUID restaurantId);
}
