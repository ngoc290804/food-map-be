package com.doan.backend.modules.restaurant.repository;

import com.doan.backend.modules.restaurant.entity.RestaurantStoreView;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RestaurantStoreViewRepository extends JpaRepository<RestaurantStoreView, UUID>,
        JpaSpecificationExecutor<RestaurantStoreView> {

    @Query("select r from RestaurantStoreView r where r.id = :id and coalesce(r.danhDauXoa, 0) = 0")
    Optional<RestaurantStoreView> findActiveById(@Param("id") UUID id);
}
