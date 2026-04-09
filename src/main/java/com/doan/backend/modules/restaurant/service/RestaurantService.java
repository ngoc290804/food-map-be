package com.doan.backend.modules.restaurant.service;

import com.doan.backend.common.dto.PageResponse;
import com.doan.backend.modules.restaurant.dto.request.RestaurantCreateRequest;
import com.doan.backend.modules.restaurant.dto.request.RestaurantUpdateRequest;
import com.doan.backend.modules.restaurant.dto.response.RestaurantResponse;
import java.util.UUID;

public interface RestaurantService {
    RestaurantResponse create(RestaurantCreateRequest request);

    RestaurantResponse update(UUID id, RestaurantUpdateRequest request);

    RestaurantResponse getDetail(UUID id);

    void delete(UUID id);

    PageResponse<RestaurantResponse> search(String keyword, int page, int size);
}
