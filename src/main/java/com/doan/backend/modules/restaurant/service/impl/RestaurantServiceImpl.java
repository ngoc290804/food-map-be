package com.doan.backend.modules.restaurant.service.impl;

import com.doan.backend.common.dto.PageResponse;
import com.doan.backend.common.enums.MenuCategory;
import com.doan.backend.common.enums.MenuDetail;
import com.doan.backend.common.exception.BadRequestException;
import com.doan.backend.common.exception.ResourceNotFoundException;
import com.doan.backend.common.util.DateTimeUtils;
import com.doan.backend.modules.restaurant.dto.request.CuaHangCreateDto;
import com.doan.backend.modules.restaurant.dto.request.CuaHangUpdateDto;
import com.doan.backend.modules.restaurant.entity.Restaurant;
import com.doan.backend.modules.restaurant.entity.RestaurantStoreView;
import com.doan.backend.modules.restaurant.repository.RestaurantRepository;
import com.doan.backend.modules.restaurant.repository.RestaurantStoreViewRepository;
import com.doan.backend.modules.restaurant.service.RestaurantService;
import com.doan.backend.modules.restaurant.specification.RestaurantStoreViewSpecification;
import com.doan.backend.modules.restaurant.vo.CuaHangVo;
import com.doan.backend.modules.upload.dto.response.UploadResponse;
import com.doan.backend.modules.upload.service.UploadService;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class RestaurantServiceImpl implements RestaurantService {

    private final RestaurantRepository restaurantRepository;
    private final RestaurantStoreViewRepository restaurantStoreViewRepository;
    private final UploadService uploadService;

    @Override
    public CuaHangVo create(CuaHangCreateDto request) {
        Restaurant entity = new Restaurant();
        applyRequest(entity, request.getTenQuanAn(), request.getDiaChi(), request.getGioMoCua(), request.getGioDongCua(),
                request.getMoTa(), request.getHinhAnh(), request.getImagePublicId(), request.getLoaiCuaHang(),
                request.getLoaiKinhDoanh());
        return mapToResponse(restaurantRepository.save(entity));
    }

    @Override
    public CuaHangVo create(CuaHangCreateDto request, MultipartFile image) {
        applyImageIfPresent(request, image);
        return create(request);
    }

    @Override
    public CuaHangVo update(UUID id, CuaHangUpdateDto request) {
        Restaurant entity = restaurantRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Khong tim thay quan an"));
        applyRequest(entity, request.getTenQuanAn(), request.getDiaChi(), request.getGioMoCua(), request.getGioDongCua(),
                request.getMoTa(), request.getHinhAnh(), request.getImagePublicId(), request.getLoaiCuaHang(),
                request.getLoaiKinhDoanh());
        entity.setStatus(request.getTrangThai());
        return mapToResponse(restaurantRepository.save(entity));
    }

    @Override
    public CuaHangVo update(UUID id, CuaHangUpdateDto request, MultipartFile image) {
        applyImageIfPresent(request, image);
        return update(id, request);
    }

    @Override
    public CuaHangVo getDetail(UUID id) {
        return restaurantStoreViewRepository.findById(id)
                .map(this::mapToResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Khong tim thay quan an"));
    }

    @Override
    public void delete(UUID id) {
        Restaurant entity = restaurantRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Khong tim thay quan an"));
        restaurantRepository.delete(entity);
    }

    @Override
    public PageResponse<CuaHangVo> search(String keyword, int page, int size) {
        return search(keyword, null, null, page, size);
    }

    @Override
    public PageResponse<CuaHangVo> search(
            String keyword,
            MenuCategory loaiCuaHang,
            MenuDetail loaiKinhDoanh,
            int page,
            int size
    ) {
        Page<RestaurantStoreView> result = restaurantStoreViewRepository.findAll(
                RestaurantStoreViewSpecification.filter(keyword, loaiCuaHang, loaiKinhDoanh),
                PageRequest.of(page, size));
        List<CuaHangVo> items = result.getContent().stream().map(this::mapToResponse).toList();
        return PageResponse.from(result, items);
    }

    private void applyRequest(
            Restaurant entity,
            String name,
            String address,
            String openTime,
            String closeTime,
            String description,
            String imageUrl,
            String imagePublicId,
            MenuCategory loaiCuaHang,
            MenuDetail loaiKinhDoanh
    ) {
        entity.setName(name);
        entity.setAddress(address);
        entity.setOpenTime(parseTime(openTime, "gioMoCua"));
        entity.setCloseTime(parseTime(closeTime, "gioDongCua"));
        entity.setDescription(description);
        entity.setImageUrl(imageUrl);
        entity.setImagePublicId(imagePublicId);
        entity.setLoaiCuaHang(loaiCuaHang == null ? null : loaiCuaHang.name());
        entity.setLoaiKinhDoanh(loaiKinhDoanh == null ? null : loaiKinhDoanh.name());
    }

    private LocalTime parseTime(String value, String fieldName) {
        try {
            return DateTimeUtils.parseTime(value);
        } catch (DateTimeParseException ex) {
            throw new BadRequestException(fieldName + " phai dung dinh dang HH:mm");
        }
    }

    private void applyImageIfPresent(CuaHangCreateDto request, MultipartFile image) {
        if (image == null || image.isEmpty()) {
            return;
        }
        UploadResponse uploadedImage = uploadService.uploadImage(image);
        request.setHinhAnh(uploadedImage.getUrl());
        request.setImagePublicId(uploadedImage.getPublicId());
    }

    private void applyImageIfPresent(CuaHangUpdateDto request, MultipartFile image) {
        if (image == null || image.isEmpty()) {
            return;
        }
        UploadResponse uploadedImage = uploadService.uploadImage(image);
        request.setHinhAnh(uploadedImage.getUrl());
        request.setImagePublicId(uploadedImage.getPublicId());
    }

    private CuaHangVo mapToResponse(RestaurantStoreView entity) {
        return CuaHangVo.builder()
                .id(entity.getId())
                .tenQuanAn(entity.getName())
                .diaChi(entity.getAddress())
                .gioMoCua(entity.getOpenTime() == null ? null : entity.getOpenTime().toString())
                .gioDongCua(entity.getCloseTime() == null ? null : entity.getCloseTime().toString())
                .moTa(entity.getDescription() == null ? entity.getAddress() : entity.getDescription())
                .hinhAnh(entity.getImageUrl())
                .imagePublicId(entity.getImagePublicId())
                .trangThai(entity.getStatus() == null ? "ACTIVE" : entity.getStatus())
                .loaiCuaHang(entity.getLoaiCuaHang())
                .loaiKinhDoanh(entity.getLoaiKinhDoanh())
                .build();
    }

    private CuaHangVo mapToResponse(Restaurant entity) {
        return CuaHangVo.builder()
                .id(entity.getId())
                .tenQuanAn(entity.getName())
                .diaChi(entity.getAddress())
                .gioMoCua(entity.getOpenTime() == null ? null : entity.getOpenTime().toString())
                .gioDongCua(entity.getCloseTime() == null ? null : entity.getCloseTime().toString())
                .moTa(entity.getDescription() == null ? entity.getAddress() : entity.getDescription())
                .hinhAnh(entity.getImageUrl())
                .imagePublicId(entity.getImagePublicId())
                .trangThai(entity.getStatus() == null ? "ACTIVE" : entity.getStatus())
                .loaiCuaHang(entity.getLoaiCuaHang())
                .loaiKinhDoanh(entity.getLoaiKinhDoanh())
                .build();
    }
}
