package com.doan.backend.modules.restaurant.specification;

import com.doan.backend.modules.restaurant.entity.RestaurantStoreView;
import org.springframework.data.jpa.domain.Specification;

public final class RestaurantStoreViewSpecification {

    private RestaurantStoreViewSpecification() {
    }

    public static Specification<RestaurantStoreView> activeKeyword(String keyword) {
        return (root, query, builder) -> {
            Specification<RestaurantStoreView> keywordSpec = (keywordRoot, keywordQuery, keywordBuilder) -> {
                if (keyword == null || keyword.isBlank()) {
                    return keywordBuilder.conjunction();
                }
                String likeValue = "%" + keyword.trim().toLowerCase() + "%";
                return keywordBuilder.or(
                        keywordBuilder.like(keywordBuilder.lower(keywordRoot.get("name")), likeValue),
                        keywordBuilder.like(keywordBuilder.lower(keywordRoot.get("address")), likeValue)
                );
            };

            return keywordSpec.toPredicate(root, query, builder);
        };
    }
}
