package com.doan.backend.modules.restaurant.specification;

import com.doan.backend.common.enums.MenuCategory;
import com.doan.backend.common.enums.MenuDetail;
import com.doan.backend.modules.restaurant.entity.RestaurantStoreView;
import org.springframework.data.jpa.domain.Specification;

public final class RestaurantStoreViewSpecification {

    private RestaurantStoreViewSpecification() {
    }

    public static Specification<RestaurantStoreView> filter(
            String keyword,
            MenuCategory loaiCuaHang,
            MenuDetail loaiKinhDoanh
    ) {
        return (root, query, builder) -> {
            var predicate = builder.conjunction();
            if (keyword != null && !keyword.isBlank()) {
                String likeValue = "%" + keyword.trim().toLowerCase() + "%";
                predicate = builder.and(predicate, builder.or(
                        builder.like(builder.lower(root.get("name")), likeValue),
                        builder.like(builder.lower(root.get("address")), likeValue),
                        builder.like(builder.lower(root.get("description")), likeValue)
                ));
            }
            if (loaiCuaHang != null) {
                predicate = builder.and(predicate,
                        builder.equal(builder.upper(root.get("loaiCuaHang")), loaiCuaHang.name()));
            }
            if (loaiKinhDoanh != null) {
                predicate = builder.and(predicate,
                        builder.equal(builder.upper(root.get("loaiKinhDoanh")), loaiKinhDoanh.name()));
            }
            return predicate;
        };
    }
}
