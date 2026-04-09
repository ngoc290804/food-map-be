package com.doan.backend.modules.restaurant.repository;

import com.doan.backend.modules.restaurant.entity.RestaurantStoreView;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface RestaurantStoreViewRepository extends JpaRepository<RestaurantStoreView, UUID>,
        JpaSpecificationExecutor<RestaurantStoreView> {
}
