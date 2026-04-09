package com.doan.backend.modules.menu.specification;

import com.doan.backend.modules.menu.entity.MenuItem;
import java.math.BigDecimal;
import org.springframework.data.jpa.domain.Specification;

public final class MenuItemSpecification {

    private MenuItemSpecification() {
    }

    public static Specification<MenuItem> filter(String keyword, String flavor, BigDecimal minPrice, BigDecimal maxPrice) {
        return (root, query, builder) -> {
            var predicate = builder.conjunction();
            if (keyword != null && !keyword.isBlank()) {
                String likeValue = "%" + keyword.trim().toLowerCase() + "%";
                predicate = builder.and(predicate, builder.or(
                        builder.like(builder.lower(root.get("name")), likeValue),
                        builder.like(builder.lower(root.get("description")), likeValue)
                ));
            }
            if (flavor != null && !flavor.isBlank()) {
                predicate = builder.and(predicate,
                        builder.equal(builder.lower(root.get("flavor")), flavor.trim().toLowerCase()));
            }
            if (minPrice != null) {
                predicate = builder.and(predicate, builder.greaterThanOrEqualTo(root.get("price"), minPrice));
            }
            if (maxPrice != null) {
                predicate = builder.and(predicate, builder.lessThanOrEqualTo(root.get("price"), maxPrice));
            }
            return predicate;
        };
    }
}
