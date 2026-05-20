package com.doan.backend.modules.favorite.controller;

import com.doan.backend.common.dto.ApiResponse;
import com.doan.backend.modules.favorite.dto.request.FavoriteRequest;
import com.doan.backend.modules.favorite.service.FavoriteService;
import com.doan.backend.modules.favorite.vo.FavoriteVo;
import jakarta.validation.Valid;
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
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/favorites")
@RequiredArgsConstructor
public class FavoriteController {

    private final FavoriteService favoriteService;

    @GetMapping
    public ApiResponse<List<FavoriteVo>> findCurrentUserFavorites() {
        return ApiResponse.success(favoriteService.findCurrentUserFavorites());
    }

    @PostMapping
    public ApiResponse<FavoriteVo> create(@Valid @RequestBody FavoriteRequest request) {
        return ApiResponse.success("Thêm vào yêu thích thành công", favoriteService.create(request));
    }

    @PutMapping("/{id}")
    public ApiResponse<FavoriteVo> update(@PathVariable UUID id, @Valid @RequestBody FavoriteRequest request) {
        return ApiResponse.success("Cập nhật yêu thích thành công", favoriteService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<String> delete(@PathVariable UUID id) {
        favoriteService.delete(id);
        return ApiResponse.success("Xóa yêu thích thành công", "Xóa yêu thích thành công");
    }

    @DeleteMapping("/restaurants/{restaurantId}")
    public ApiResponse<String> deleteByRestaurant(@PathVariable UUID restaurantId) {
        favoriteService.deleteByRestaurant(restaurantId);
        return ApiResponse.success("Xóa yêu thích thành công", "Xóa yêu thích thành công");
    }
}
