package com.doan.backend.modules.menu.dto.response;

import java.math.BigDecimal;
import java.util.UUID;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class MenuItemResponse {
    private final UUID id;
    private final UUID restaurantId;
    private final String restaurantName;
    private final String name;
    private final BigDecimal price;
    private final String flavor;
    private final String description;
    private final String imageUrl;
    private final Boolean available;
}
