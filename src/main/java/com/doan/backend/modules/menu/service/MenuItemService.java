package com.doan.backend.modules.menu.service;

import com.doan.backend.common.dto.PageResponse;
import com.doan.backend.modules.menu.dto.request.MenuItemCreateRequest;
import com.doan.backend.modules.menu.dto.request.MenuItemUpdateRequest;
import com.doan.backend.modules.menu.dto.response.MenuItemResponse;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public interface MenuItemService {
    MenuItemResponse create(MenuItemCreateRequest request);

    MenuItemResponse update(UUID id, MenuItemUpdateRequest request);

    MenuItemResponse getDetail(UUID id);

    void delete(UUID id);

    PageResponse<MenuItemResponse> search(String keyword, String flavor, BigDecimal minPrice, BigDecimal maxPrice,
                                          int page, int size);

    List<MenuItemResponse> findByRestaurant(UUID restaurantId);
}
