package com.doan.backend.modules.menu.repository;

import com.doan.backend.modules.menu.entity.MenuItem;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

public interface MenuItemRepository extends JpaRepository<MenuItem, UUID>, JpaSpecificationExecutor<MenuItem> {
    List<MenuItem> findByRestaurantId(UUID restaurantId);

    @Query("""
            select m from MenuItem m
            join m.restaurant r
            where coalesce(r.danhDauXoa, 0) = 0
            order by r.createdAt desc, m.createdAt desc
            """)
    List<MenuItem> findActiveRestaurantMenuItems();
}
