package com.doan.backend.modules.favorite.service.impl;

import com.doan.backend.common.exception.BadRequestException;
import com.doan.backend.common.exception.ResourceNotFoundException;
import com.doan.backend.common.exception.UnauthorizedException;
import com.doan.backend.modules.favorite.dto.request.FavoriteRequest;
import com.doan.backend.modules.favorite.entity.Favorite;
import com.doan.backend.modules.favorite.repository.FavoriteRepository;
import com.doan.backend.modules.favorite.service.FavoriteService;
import com.doan.backend.modules.favorite.vo.FavoriteVo;
import com.doan.backend.modules.restaurant.entity.Restaurant;
import com.doan.backend.modules.restaurant.repository.RestaurantRepository;
import com.doan.backend.modules.user.entity.User;
import com.doan.backend.modules.user.repository.UserRepository;
import com.doan.backend.security.SecurityUtils;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class FavoriteServiceImpl implements FavoriteService {

    private final FavoriteRepository favoriteRepository;
    private final RestaurantRepository restaurantRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public List<FavoriteVo> findCurrentUserFavorites() {
        UUID userId = getCurrentUserId();
        return favoriteRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .filter(favorite -> !isDeleted(favorite.getRestaurant()))
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    @Transactional
    public FavoriteVo create(FavoriteRequest request) {
        UUID userId = getCurrentUserId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy tài khoản"));
        Restaurant restaurant = restaurantRepository.findActiveById(request.getIdCuaHang())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy quán ăn"));
        if (favoriteRepository.existsByUserIdAndRestaurantId(userId, request.getIdCuaHang())) {
            throw new BadRequestException("Quán ăn đã có trong danh sách yêu thích");
        }

        Favorite favorite = new Favorite();
        favorite.setUser(user);
        favorite.setRestaurant(restaurant);
        return mapToResponse(favoriteRepository.save(favorite));
    }

    @Override
    @Transactional
    public FavoriteVo update(UUID id, FavoriteRequest request) {
        UUID userId = getCurrentUserId();
        Favorite favorite = favoriteRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy mục yêu thích"));

        Restaurant restaurant = restaurantRepository.findActiveById(request.getIdCuaHang())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy quán ăn"));
        favoriteRepository.findByUserIdAndRestaurantId(userId, request.getIdCuaHang())
                .filter(existing -> !existing.getId().equals(id))
                .ifPresent(existing -> {
                    throw new BadRequestException("Quán ăn đã có trong danh sách yêu thích");
                });
        favorite.setRestaurant(restaurant);
        return mapToResponse(favoriteRepository.save(favorite));
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        UUID userId = getCurrentUserId();
        Favorite favorite = favoriteRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy mục yêu thích"));
        favoriteRepository.delete(favorite);
    }

    @Override
    @Transactional
    public void deleteByRestaurant(UUID restaurantId) {
        UUID userId = getCurrentUserId();
        Favorite favorite = favoriteRepository.findByUserIdAndRestaurantId(userId, restaurantId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy mục yêu thích"));
        favoriteRepository.delete(favorite);
    }

    private UUID getCurrentUserId() {
        return SecurityUtils.getCurrentUser()
                .map(user -> user.getId())
                .orElseThrow(() -> new UnauthorizedException("Vui lòng đăng nhập để thực hiện thao tác này"));
    }

    private FavoriteVo mapToResponse(Favorite favorite) {
        Restaurant restaurant = favorite.getRestaurant();
        User user = favorite.getUser();
        return FavoriteVo.builder()
                .id(favorite.getId())
                .idCuaHang(restaurant == null ? null : restaurant.getId())
                .idTaiKhoan(user == null ? null : user.getId())
                .tenQuanAn(restaurant == null ? null : restaurant.getName())
                .diaChi(restaurant == null ? null : restaurant.getAddress())
                .hinhAnh(restaurant == null ? null : restaurant.getImageUrl())
                .ngayTao(favorite.getCreatedAt())
                .ngaySua(favorite.getUpdatedAt())
                .build();
    }

    private boolean isDeleted(Restaurant restaurant) {
        return restaurant != null && Integer.valueOf(1).equals(restaurant.getDanhDauXoa());
    }
}
