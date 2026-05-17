package com.doan.backend.modules.restaurant.controller;

import com.doan.backend.common.dto.ApiResponse;
import com.doan.backend.common.dto.PageResponse;
import com.doan.backend.common.enums.MenuCategory;
import com.doan.backend.common.enums.MenuDetail;
import com.doan.backend.modules.restaurant.dto.request.CuaHangCreateDto;
import com.doan.backend.modules.restaurant.dto.request.CuaHangUpdateDto;
import com.doan.backend.modules.restaurant.service.RestaurantService;
import com.doan.backend.modules.restaurant.vo.CuaHangVo;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/restaurants")
@RequiredArgsConstructor
public class RestaurantController {

    private final RestaurantService restaurantService;

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ApiResponse<CuaHangVo> create(@Valid @RequestBody CuaHangCreateDto request) {
        return ApiResponse.success(restaurantService.create(request));
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<CuaHangVo> createWithImage(
            @Valid @ModelAttribute CuaHangCreateDto request,
            @RequestPart(required = false) MultipartFile image,
            @RequestPart(required = false) MultipartFile file
    ) {
        return ApiResponse.success(restaurantService.create(request, resolveImage(image, file)));
    }

    @PutMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ApiResponse<CuaHangVo> update(
            @PathVariable UUID id,
            @Valid @RequestBody CuaHangUpdateDto request
    ) {
        return ApiResponse.success(restaurantService.update(id, request));
    }

    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<CuaHangVo> updateWithImage(
            @PathVariable UUID id,
            @Valid @ModelAttribute CuaHangUpdateDto request,
            @RequestPart(required = false) MultipartFile image,
            @RequestPart(required = false) MultipartFile file
    ) {
        return ApiResponse.success(restaurantService.update(id, request, resolveImage(image, file)));
    }

    @GetMapping("/{id}")
    public ApiResponse<CuaHangVo> getDetail(@PathVariable UUID id) {
        return ApiResponse.success(restaurantService.getDetail(id));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<String> delete(@PathVariable UUID id) {
        restaurantService.delete(id);
        return ApiResponse.success("Delete successful", "Delete successful");
    }

    @GetMapping
    public ApiResponse<PageResponse<CuaHangVo>> search(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) MenuCategory loaiCuaHang,
            @RequestParam(required = false) MenuDetail loaiKinhDoanh,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ApiResponse.success(restaurantService.search(keyword, loaiCuaHang, loaiKinhDoanh, page, size));
    }

    private MultipartFile resolveImage(MultipartFile image, MultipartFile file) {
        return image == null || image.isEmpty() ? file : image;
    }
}
