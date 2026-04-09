package com.doan.backend.modules.restaurant.controller;

import com.doan.backend.common.dto.ApiResponse;
import com.doan.backend.common.dto.PageResponse;
import com.doan.backend.modules.restaurant.dto.request.RestaurantCreateRequest;
import com.doan.backend.modules.restaurant.dto.request.RestaurantUpdateRequest;
import com.doan.backend.modules.restaurant.dto.response.RestaurantResponse;
import com.doan.backend.modules.restaurant.service.RestaurantService;
import jakarta.validation.Valid;
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
@RequestMapping("/api/restaurants")
@RequiredArgsConstructor
public class RestaurantController {

    private final RestaurantService restaurantService;

    @PostMapping
    public ApiResponse<RestaurantResponse> create(@Valid @RequestBody RestaurantCreateRequest request) {
        return ApiResponse.success(restaurantService.create(request));
    }

    @PutMapping("/{id}")
    public ApiResponse<RestaurantResponse> update(
            @PathVariable UUID id,
            @Valid @RequestBody RestaurantUpdateRequest request
    ) {
        return ApiResponse.success(restaurantService.update(id, request));
    }

    @GetMapping("/{id}")
    public ApiResponse<RestaurantResponse> getDetail(@PathVariable UUID id) {
        return ApiResponse.success(restaurantService.getDetail(id));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<String> delete(@PathVariable UUID id) {
        restaurantService.delete(id);
        return ApiResponse.success("Delete successful", "Delete successful");
    }

    @GetMapping
    public ApiResponse<PageResponse<RestaurantResponse>> search(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ApiResponse.success(restaurantService.search(keyword, page, size));
    }
}
