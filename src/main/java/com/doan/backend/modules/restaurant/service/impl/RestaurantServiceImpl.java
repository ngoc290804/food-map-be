package com.doan.backend.modules.restaurant.service.impl;

import com.doan.backend.common.dto.PageResponse;
import com.doan.backend.common.exception.BadRequestException;
import com.doan.backend.common.exception.ResourceNotFoundException;
import com.doan.backend.modules.restaurant.dto.request.RestaurantCreateRequest;
import com.doan.backend.modules.restaurant.dto.request.RestaurantUpdateRequest;
import com.doan.backend.modules.restaurant.dto.response.RestaurantResponse;
import com.doan.backend.modules.restaurant.entity.RestaurantStoreView;
import com.doan.backend.modules.restaurant.repository.RestaurantStoreViewRepository;
import com.doan.backend.modules.restaurant.service.RestaurantService;
import com.doan.backend.modules.restaurant.specification.RestaurantStoreViewSpecification;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RestaurantServiceImpl implements RestaurantService {

    private static final String READ_ONLY_MESSAGE = "Che do XAMPP tam thoi chi ho tro doc danh sach quan an";

    private final RestaurantStoreViewRepository restaurantStoreViewRepository;

    @Override
    public RestaurantResponse create(RestaurantCreateRequest request) {
        throw new BadRequestException(READ_ONLY_MESSAGE);
    }

    @Override
    public RestaurantResponse update(UUID id, RestaurantUpdateRequest request) {
        throw new BadRequestException(READ_ONLY_MESSAGE);
    }

    @Override
    public RestaurantResponse getDetail(UUID id) {
        return restaurantStoreViewRepository.findById(id)
                .map(this::mapToResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Khong tim thay quan an"));
    }

    @Override
    public void delete(UUID id) {
        throw new BadRequestException(READ_ONLY_MESSAGE);
    }

    @Override
    public PageResponse<RestaurantResponse> search(String keyword, int page, int size) {
        Page<RestaurantStoreView> result = restaurantStoreViewRepository.findAll(
                RestaurantStoreViewSpecification.activeKeyword(keyword),
                PageRequest.of(page, size));
        List<RestaurantResponse> items = result.getContent().stream().map(this::mapToResponse).toList();
        return PageResponse.from(result, items);
    }

    private RestaurantResponse mapToResponse(RestaurantStoreView entity) {
        return RestaurantResponse.builder()
                .id(entity.getId())
                .name(entity.getName())
                .address(entity.getAddress())
                .openTime(entity.getOpenTime() == null ? null : entity.getOpenTime().toString())
                .closeTime(entity.getCloseTime() == null ? null : entity.getCloseTime().toString())
                .description(entity.getDescription() == null ? entity.getAddress() : entity.getDescription())
                .imageUrl(entity.getImageUrl())
                .status(entity.getStatus() == null ? "ACTIVE" : entity.getStatus())
                .build();
    }
}
