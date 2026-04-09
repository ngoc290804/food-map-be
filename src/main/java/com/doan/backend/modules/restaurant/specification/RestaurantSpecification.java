package com.doan.backend.modules.restaurant.specification;

import com.doan.backend.modules.restaurant.entity.Restaurant;
import org.springframework.data.jpa.domain.Specification;

public final class RestaurantSpecification {

    private RestaurantSpecification() {
    }

    public static Specification<Restaurant> keyword(String keyword) {
        return (root, query, builder) -> {
            if (keyword == null || keyword.isBlank()) {
                return builder.conjunction();
            }
            String likeValue = "%" + keyword.trim().toLowerCase() + "%";
            return builder.or(
                    builder.like(builder.lower(root.get("name")), likeValue),
                    builder.like(builder.lower(root.get("address")), likeValue)
            );
        };
    }
}
