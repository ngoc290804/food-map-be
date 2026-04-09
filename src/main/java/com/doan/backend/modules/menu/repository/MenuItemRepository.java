package com.doan.backend.modules.menu.repository;

import com.doan.backend.modules.menu.entity.MenuItem;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface MenuItemRepository extends JpaRepository<MenuItem, UUID>, JpaSpecificationExecutor<MenuItem> {
    List<MenuItem> findByRestaurantId(UUID restaurantId);
}
