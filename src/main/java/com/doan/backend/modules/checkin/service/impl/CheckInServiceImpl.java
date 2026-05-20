package com.doan.backend.modules.checkin.service.impl;

import com.doan.backend.common.exception.ResourceNotFoundException;
import com.doan.backend.common.exception.UnauthorizedException;
import com.doan.backend.modules.checkin.dto.request.CheckInRequest;
import com.doan.backend.modules.checkin.entity.CheckIn;
import com.doan.backend.modules.checkin.repository.CheckInRepository;
import com.doan.backend.modules.checkin.service.CheckInService;
import com.doan.backend.modules.checkin.vo.CheckInVo;
import com.doan.backend.modules.restaurant.entity.Restaurant;
import com.doan.backend.modules.restaurant.repository.RestaurantRepository;
import com.doan.backend.modules.user.entity.User;
import com.doan.backend.modules.user.repository.UserRepository;
import com.doan.backend.security.SecurityUtils;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CheckInServiceImpl implements CheckInService {

    private static final int ALLOWED_DISTANCE_METERS = 200;
    private static final double EARTH_RADIUS_METERS = 6371000D;

    private final CheckInRepository checkInRepository;
    private final RestaurantRepository restaurantRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public CheckInVo checkIn(CheckInRequest request) {
        UUID userId = getCurrentUserId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy tài khoản"));
        Restaurant restaurant = restaurantRepository.findActiveById(request.getIdQuanAn())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy quán ăn"));

        Double distance = calculateDistanceMeters(
                request.getLatitude(),
                request.getLongitude(),
                restaurant.getLatitude(),
                restaurant.getLongitude());
        boolean success = distance != null && distance <= ALLOWED_DISTANCE_METERS;

        CheckIn checkIn = new CheckIn();
        checkIn.setUser(user);
        checkIn.setRestaurant(restaurant);
        checkIn.setCheckIn(success ? 1 : 0);
        return mapToResponse(checkInRepository.save(checkIn), request.getLatitude(), request.getLongitude(), distance);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CheckInVo> findCurrentUserCheckIns() {
        UUID userId = getCurrentUserId();
        return checkInRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .filter(checkIn -> checkIn.getRestaurant() == null
                        || !Integer.valueOf(1).equals(checkIn.getRestaurant().getDanhDauXoa()))
                .map(checkIn -> mapToResponse(checkIn, null, null, null))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<CheckInVo> findByRestaurant(UUID restaurantId) {
        if (!restaurantRepository.existsActiveById(restaurantId)) {
            throw new ResourceNotFoundException("Không tìm thấy quán ăn");
        }
        return checkInRepository.findByRestaurantIdOrderByCreatedAtDesc(restaurantId).stream()
                .map(checkIn -> mapToResponse(checkIn, null, null, null))
                .toList();
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        UUID userId = getCurrentUserId();
        CheckIn checkIn = checkInRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy check-in"));
        checkInRepository.delete(checkIn);
    }

    private UUID getCurrentUserId() {
        return SecurityUtils.getCurrentUser()
                .map(user -> user.getId())
                .orElseThrow(() -> new UnauthorizedException("Vui lòng đăng nhập để check-in"));
    }

    private Double calculateDistanceMeters(
            BigDecimal currentLatitude,
            BigDecimal currentLongitude,
            BigDecimal restaurantLatitude,
            BigDecimal restaurantLongitude
    ) {
        if (restaurantLatitude == null || restaurantLongitude == null) {
            return null;
        }

        double lat1 = Math.toRadians(currentLatitude.doubleValue());
        double lon1 = Math.toRadians(currentLongitude.doubleValue());
        double lat2 = Math.toRadians(restaurantLatitude.doubleValue());
        double lon2 = Math.toRadians(restaurantLongitude.doubleValue());

        double deltaLat = lat2 - lat1;
        double deltaLon = lon2 - lon1;
        double haversine = Math.pow(Math.sin(deltaLat / 2), 2)
                + Math.cos(lat1) * Math.cos(lat2) * Math.pow(Math.sin(deltaLon / 2), 2);
        return 2 * EARTH_RADIUS_METERS * Math.atan2(Math.sqrt(haversine), Math.sqrt(1 - haversine));
    }

    private CheckInVo mapToResponse(
            CheckIn checkIn,
            BigDecimal currentLatitude,
            BigDecimal currentLongitude,
            Double distance
    ) {
        User user = checkIn.getUser();
        Restaurant restaurant = checkIn.getRestaurant();
        return CheckInVo.builder()
                .id(checkIn.getId())
                .idTaiKhoan(user == null ? null : user.getId())
                .idQuanAn(restaurant == null ? null : restaurant.getId())
                .tenQuanAn(restaurant == null ? null : restaurant.getName())
                .checkin(checkIn.getCheckIn())
                .thanhCong(Integer.valueOf(1).equals(checkIn.getCheckIn()))
                .latitudeHienTai(currentLatitude)
                .longitudeHienTai(currentLongitude)
                .latitudeQuanAn(restaurant == null ? null : restaurant.getLatitude())
                .longitudeQuanAn(restaurant == null ? null : restaurant.getLongitude())
                .khoangCachMet(distance == null ? null : Math.round(distance * 10D) / 10D)
                .nguongChoPhepMet(ALLOWED_DISTANCE_METERS)
                .ngayTao(checkIn.getCreatedAt())
                .build();
    }
}
