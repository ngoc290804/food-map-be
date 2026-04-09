package com.doan.backend.modules.menu.controller;

import com.doan.backend.common.dto.ApiResponse;
import com.doan.backend.common.dto.PageResponse;
import com.doan.backend.modules.menu.dto.request.MenuItemCreateRequest;
import com.doan.backend.modules.menu.dto.request.MenuItemUpdateRequest;
import com.doan.backend.modules.menu.dto.response.MenuItemResponse;
import com.doan.backend.modules.menu.service.MenuItemService;
import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class MenuItemController {

    private final MenuItemService menuItemService;

    @GetMapping("/menu-items")
    public ApiResponse<PageResponse<MenuItemResponse>> search(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String flavor,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ApiResponse.success(menuItemService.search(keyword, flavor, minPrice, maxPrice, page, size));
    }

    @GetMapping("/menu-items/{id}")
    public ApiResponse<MenuItemResponse> getDetail(@PathVariable UUID id) {
        return ApiResponse.success(menuItemService.getDetail(id));
    }

    @GetMapping("/restaurants/{restaurantId}/menu-items")
    public ApiResponse<List<MenuItemResponse>> getByRestaurant(@PathVariable UUID restaurantId) {
        return ApiResponse.success(menuItemService.findByRestaurant(restaurantId));
    }

    @PostMapping("/menu-items")
    public ApiResponse<MenuItemResponse> create(@Valid @RequestBody MenuItemCreateRequest request) {
        return ApiResponse.success(menuItemService.create(request));
    }

    @PutMapping("/menu-items/{id}")
    public ApiResponse<MenuItemResponse> update(
            @PathVariable UUID id,
            @Valid @RequestBody MenuItemUpdateRequest request
    ) {
        return ApiResponse.success(menuItemService.update(id, request));
    }

    @DeleteMapping("/menu-items/{id}")
    public ApiResponse<String> delete(@PathVariable UUID id) {
        menuItemService.delete(id);
        return ApiResponse.success("Delete successful", "Delete successful");
    }
}
