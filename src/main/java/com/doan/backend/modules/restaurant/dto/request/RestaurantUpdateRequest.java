package com.doan.backend.modules.restaurant.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RestaurantUpdateRequest {

    @NotBlank(message = "Tên quán ăn không được để trống")
    @Size(max = 255)
    private String name;

    @NotBlank(message = "Địa chỉ không được để trống")
    @Size(max = 500)
    private String address;

    private String openTime;
    private String closeTime;

    @Size(max = 1000)
    private String description;

    private String imageUrl;
    private String status;
}
