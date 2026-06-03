package com.doan.backend.modules.restaurant.service.impl;

import com.doan.backend.common.dto.PageResponse;
import com.doan.backend.common.enums.AccountType;
import com.doan.backend.common.enums.MenuCategory;
import com.doan.backend.common.enums.MenuDetail;
import com.doan.backend.common.exception.BadRequestException;
import com.doan.backend.common.exception.ResourceNotFoundException;
import com.doan.backend.common.exception.UnauthorizedException;
import com.doan.backend.common.util.DateTimeUtils;
import com.doan.backend.modules.favorite.entity.Favorite;
import com.doan.backend.modules.favorite.repository.FavoriteRepository;
import com.doan.backend.modules.restaurant.dto.request.CuaHangCreateDto;
import com.doan.backend.modules.restaurant.dto.request.CuaHangUpdateDto;
import com.doan.backend.modules.restaurant.entity.Restaurant;
import com.doan.backend.modules.restaurant.entity.RestaurantStoreView;
import com.doan.backend.modules.restaurant.repository.RestaurantRepository;
import com.doan.backend.modules.restaurant.repository.RestaurantStoreViewRepository;
import com.doan.backend.modules.restaurant.service.GeocodingService;
import com.doan.backend.modules.restaurant.service.RestaurantService;
import com.doan.backend.modules.restaurant.specification.RestaurantStoreViewSpecification;
import com.doan.backend.modules.restaurant.vo.CuaHangVo;
import com.doan.backend.modules.review.service.ReviewService;
import com.doan.backend.modules.review.vo.ReviewSummaryVo;
import com.doan.backend.modules.upload.dto.response.UploadResponse;
import com.doan.backend.modules.upload.service.UploadService;
import com.doan.backend.security.CustomUserDetails;
import com.doan.backend.security.SecurityUtils;
import java.util.Objects;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class RestaurantServiceImpl implements RestaurantService {

    private final RestaurantRepository restaurantRepository;
    private final RestaurantStoreViewRepository restaurantStoreViewRepository;
    private final UploadService uploadService;
    private final GeocodingService geocodingService;
    private final FavoriteRepository favoriteRepository;
    private final ReviewService reviewService;

    @Override
    public CuaHangVo create(CuaHangCreateDto request) {
        Optional<CustomUserDetails> currentUser = SecurityUtils.getCurrentUser();
        if (isStoreAccount(currentUser)) {
            UUID ownerId = currentUser.map(CustomUserDetails::getId)
                    .orElseThrow(() -> new UnauthorizedException("Vui long dang nhap de tao cua hang"));
            Optional<Restaurant> existingRestaurant = restaurantRepository.findByIdChuCuaHang(ownerId);
            if (existingRestaurant.isPresent()) {
                Restaurant entity = existingRestaurant.get();
                applyRequest(entity, request.getTenQuanAn(), request.getDiaChi(), request.getGioMoCua(),
                        request.getGioDongCua(), request.getMoTa(), request.getHinhAnh(), request.getImagePublicId(),
                        request.getLoaiCuaHang(), request.getLoaiKinhDoanh());
                entity.setIdChuCuaHang(ownerId);
                entity.setDanhDauXoa(0);
                return mapToResponse(restaurantRepository.save(entity));
            }
        }

        Restaurant entity = new Restaurant();
        applyRequest(entity, request.getTenQuanAn(), request.getDiaChi(), request.getGioMoCua(), request.getGioDongCua(),
                request.getMoTa(), request.getHinhAnh(), request.getImagePublicId(), request.getLoaiCuaHang(),
                request.getLoaiKinhDoanh());
        currentUser
                .filter(this::isStoreAccount)
                .map(CustomUserDetails::getId)
                .ifPresent(entity::setIdChuCuaHang);
        entity.setDanhDauXoa(0);
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
        ensureStoreOwnerCanEdit(entity);
        applyRequest(entity, request.getTenQuanAn(), request.getDiaChi(), request.getGioMoCua(), request.getGioDongCua(),
                request.getMoTa(), request.getHinhAnh(), request.getImagePublicId(), request.getLoaiCuaHang(),
                request.getLoaiKinhDoanh());
        entity.setStatus(request.getTrangThai());
        entity.setDanhDauXoa(0);
        return mapToResponse(restaurantRepository.save(entity));
    }

    @Override
    public CuaHangVo update(UUID id, CuaHangUpdateDto request, MultipartFile image) {
        applyImageIfPresent(request, image);
        return update(id, request);
    }

    @Override
    public CuaHangVo getDetail(UUID id) {
        return restaurantStoreViewRepository.findActiveById(id)
                .map(this::mapToResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Khong tim thay quan an"));
    }

    @Override
    public CuaHangVo getByOwnerId(UUID idTaiKhoan) {
        return restaurantStoreViewRepository.findActiveByOwnerId(idTaiKhoan)
                .map(this::mapToResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Khong tim thay cua hang cua tai khoan nay"));
    }

    @Override
    public CuaHangVo getMyRestaurant() {
        UUID ownerId = SecurityUtils.getCurrentUser()
                .map(CustomUserDetails::getId)
                .orElseThrow(() -> new UnauthorizedException("Vui long dang nhap de xem cua hang"));
        return getByOwnerId(ownerId);
    }

    @Override
    public void delete(UUID id) {
        Restaurant entity = restaurantRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Khong tim thay quan an"));
        entity.setDanhDauXoa(1);
        restaurantRepository.save(entity);
    }

    @Override
    public PageResponse<CuaHangVo> search(String keyword, int page, int size) {
        return search(keyword, null, null, page, size);
    }

    @Override
    public PageResponse<CuaHangVo> ranking(int page, int size) {
        int safePage = Math.max(page, 0);
        int safeSize = size <= 0 ? 10 : Math.min(size, 50);
        Page<RestaurantStoreView> result = restaurantStoreViewRepository.findRanking(
                PageRequest.of(safePage, safeSize));
        List<CuaHangVo> items = result.getContent().stream().map(this::mapToResponse).toList();
        return PageResponse.from(result, items);
    }

    @Override
    public PageResponse<CuaHangVo> search(
            String keyword,
            MenuCategory loaiCuaHang,
            MenuDetail loaiKinhDoanh,
            int page,
            int size
    ) {
        if (loaiCuaHang == MenuCategory.YEU_THICH) {
            return searchFavorites(keyword, loaiKinhDoanh, page, size);
        }

        Page<RestaurantStoreView> result = restaurantStoreViewRepository.findAll(
                RestaurantStoreViewSpecification.filter(keyword, loaiCuaHang, loaiKinhDoanh),
                PageRequest.of(page, size, Sort.by(
                        Sort.Order.desc("createdAt"),
                        Sort.Order.desc("id")
                )));
        List<CuaHangVo> items = result.getContent().stream().map(this::mapToResponse).toList();
        return PageResponse.from(result, items);
    }

    @Override
    public List<CuaHangVo> findActiveForChatbot(int limit) {
        int safeLimit = limit <= 0 ? 50 : Math.min(limit, 100);
        return restaurantStoreViewRepository.findActiveRestaurants().stream()
                .limit(safeLimit)
                .map(this::mapToResponse)
                .toList();
    }

    private PageResponse<CuaHangVo> searchFavorites(
            String keyword,
            MenuDetail loaiKinhDoanh,
            int page,
            int size
    ) {
        UUID userId = SecurityUtils.getCurrentUser()
                .map(user -> user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Vui long dang nhap de xem danh sach yeu thich"));
        List<Restaurant> restaurants = favoriteRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(Favorite::getRestaurant)
                .filter(Objects::nonNull)
                .filter(restaurant -> !isDeleted(restaurant))
                .filter(restaurant -> matchesFavoriteFilter(restaurant, keyword, loaiKinhDoanh))
                .toList();
        int safeSize = size <= 0 ? 10 : size;
        int safePage = Math.max(page, 0);
        int fromIndex = Math.min(safePage * safeSize, restaurants.size());
        int toIndex = Math.min(fromIndex + safeSize, restaurants.size());
        List<CuaHangVo> items = restaurants.subList(fromIndex, toIndex).stream()
                .map(this::mapToResponse)
                .toList();

        return PageResponse.<CuaHangVo>builder()
                .page(safePage)
                .size(safeSize)
                .totalElements(restaurants.size())
                .totalPages((int) Math.ceil((double) restaurants.size() / safeSize))
                .items(items)
                .build();
    }

    private boolean matchesFavoriteFilter(Restaurant restaurant, String keyword, MenuDetail loaiKinhDoanh) {
        if (loaiKinhDoanh != null && !loaiKinhDoanh.name().equals(restaurant.getLoaiKinhDoanh())) {
            return false;
        }

        if (keyword == null || keyword.trim().isEmpty()) {
            return true;
        }

        String likeValue = keyword.trim().toLowerCase(Locale.ROOT);
        return containsIgnoreCase(restaurant.getName(), likeValue)
                || containsIgnoreCase(restaurant.getAddress(), likeValue)
                || containsIgnoreCase(restaurant.getDescription(), likeValue);
    }

    private boolean isDeleted(Restaurant restaurant) {
        return Integer.valueOf(1).equals(restaurant.getDanhDauXoa());
    }

    private boolean containsIgnoreCase(String value, String keyword) {
        return value != null && value.toLowerCase(Locale.ROOT).contains(keyword);
    }

    private boolean isStoreAccount(Optional<CustomUserDetails> currentUser) {
        return currentUser
                .map(this::isStoreAccount)
                .orElse(false);
    }

    private boolean isStoreAccount(CustomUserDetails userDetails) {
        return userDetails.getAuthorities().stream()
                .anyMatch(authority -> AccountType.CUA_HANG.getValue().equals(authority.getAuthority()));
    }

    private void ensureStoreOwnerCanEdit(Restaurant entity) {
        Optional<CustomUserDetails> currentUser = SecurityUtils.getCurrentUser();
        if (!isStoreAccount(currentUser)) {
            return;
        }

        UUID ownerId = currentUser.map(CustomUserDetails::getId)
                .orElseThrow(() -> new UnauthorizedException("Vui long dang nhap de cap nhat cua hang"));
        if (!ownerId.equals(entity.getIdChuCuaHang())) {
            throw new UnauthorizedException("Tai khoan cuaHang chi duoc cap nhat cua hang cua minh");
        }
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
        String previousAddress = entity.getAddress();
        entity.setName(name);
        entity.setAddress(address);
        entity.setOpenTime(parseTime(openTime, "gioMoCua"));
        entity.setCloseTime(parseTime(closeTime, "gioDongCua"));
        entity.setDescription(description);
        entity.setImageUrl(imageUrl);
        entity.setImagePublicId(imagePublicId);
        entity.setLoaiCuaHang(loaiCuaHang == null ? null : loaiCuaHang.name());
        entity.setLoaiKinhDoanh(loaiKinhDoanh == null ? null : loaiKinhDoanh.name());
        updateCoordinates(entity, previousAddress, address);
    }

    private void updateCoordinates(Restaurant entity, String previousAddress, String address) {
        boolean shouldGeocode = entity.getLatitude() == null
                || entity.getLongitude() == null
                || !Objects.equals(previousAddress, address);
        if (!shouldGeocode) {
            return;
        }
        geocodingService.geocode(address).ifPresent(location -> {
            entity.setLatitude(location.getLatitude());
            entity.setLongitude(location.getLongitude());
        });
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
        ReviewSummaryVo reviewSummary = reviewService.getSummary(entity.getId());
        return CuaHangVo.builder()
                .id(entity.getId())
                .tenQuanAn(entity.getName())
                .diaChi(entity.getAddress())
                .gioMoCua(entity.getOpenTime() == null ? null : entity.getOpenTime().toString())
                .gioDongCua(entity.getCloseTime() == null ? null : entity.getCloseTime().toString())
                .moTa(entity.getDescription() == null ? entity.getAddress() : entity.getDescription())
                .hinhAnh(entity.getImageUrl())
                .imagePublicId(entity.getImagePublicId())
                .latitude(entity.getLatitude())
                .longitude(entity.getLongitude())
                .yeuThich(isFavorite(entity.getId()))
                .diemDanhGiaTrungBinh(reviewSummary.getDiemDanhGiaTrungBinh())
                .soLuongDanhGia(reviewSummary.getSoLuongDanhGia())
                .idChuCuaHang(entity.getIdChuCuaHang())
                .trangThai(entity.getStatus() == null ? "ACTIVE" : entity.getStatus())
                .loaiCuaHang(entity.getLoaiCuaHang())
                .loaiKinhDoanh(entity.getLoaiKinhDoanh())
                .build();
    }

    private CuaHangVo mapToResponse(Restaurant entity) {
        ReviewSummaryVo reviewSummary = reviewService.getSummary(entity.getId());
        return CuaHangVo.builder()
                .id(entity.getId())
                .tenQuanAn(entity.getName())
                .diaChi(entity.getAddress())
                .gioMoCua(entity.getOpenTime() == null ? null : entity.getOpenTime().toString())
                .gioDongCua(entity.getCloseTime() == null ? null : entity.getCloseTime().toString())
                .moTa(entity.getDescription() == null ? entity.getAddress() : entity.getDescription())
                .hinhAnh(entity.getImageUrl())
                .imagePublicId(entity.getImagePublicId())
                .latitude(entity.getLatitude())
                .longitude(entity.getLongitude())
                .yeuThich(isFavorite(entity.getId()))
                .diemDanhGiaTrungBinh(reviewSummary.getDiemDanhGiaTrungBinh())
                .soLuongDanhGia(reviewSummary.getSoLuongDanhGia())
                .idChuCuaHang(entity.getIdChuCuaHang())
                .trangThai(entity.getStatus() == null ? "ACTIVE" : entity.getStatus())
                .loaiCuaHang(entity.getLoaiCuaHang())
                .loaiKinhDoanh(entity.getLoaiKinhDoanh())
                .build();
    }

    private boolean isFavorite(UUID restaurantId) {
        Optional<UUID> userId = SecurityUtils.getCurrentUser().map(user -> user.getId());

        return userId
                .map(id -> favoriteRepository.existsByUserIdAndRestaurantId(id, restaurantId))
                .orElse(false);
    }
}
