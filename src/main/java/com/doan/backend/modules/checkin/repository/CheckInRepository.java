package com.doan.backend.modules.checkin.repository;

import com.doan.backend.modules.checkin.entity.CheckIn;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CheckInRepository extends JpaRepository<CheckIn, UUID> {

    List<CheckIn> findByUserIdOrderByCreatedAtDesc(UUID userId);

    Optional<CheckIn> findByIdAndUserId(UUID id, UUID userId);

    List<CheckIn> findByRestaurantIdOrderByCreatedAtDesc(UUID restaurantId);
}
