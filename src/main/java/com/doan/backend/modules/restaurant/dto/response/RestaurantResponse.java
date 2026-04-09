package com.doan.backend.modules.restaurant.dto.response;

import java.util.UUID;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class RestaurantResponse {
    private final UUID id;
    private final String name;
    private final String address;
    private final String openTime;
    private final String closeTime;
    private final String description;
    private final String imageUrl;
    private final String status;
}
