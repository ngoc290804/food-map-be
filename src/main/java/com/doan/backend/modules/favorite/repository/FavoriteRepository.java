package com.doan.backend.modules.favorite.repository;

import com.doan.backend.modules.favorite.entity.Favorite;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FavoriteRepository extends JpaRepository<Favorite, UUID> {

    List<Favorite> findByUserIdOrderByCreatedAtDesc(UUID userId);

    Optional<Favorite> findByIdAndUserId(UUID id, UUID userId);

    Optional<Favorite> findByUserIdAndRestaurantId(UUID userId, UUID restaurantId);

    boolean existsByUserIdAndRestaurantId(UUID userId, UUID restaurantId);
}
