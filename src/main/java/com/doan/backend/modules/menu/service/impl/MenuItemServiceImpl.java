package com.doan.backend.modules.menu.service.impl;

import com.doan.backend.common.dto.PageResponse;
import com.doan.backend.common.exception.ResourceNotFoundException;
import com.doan.backend.modules.menu.dto.request.MenuItemCreateRequest;
import com.doan.backend.modules.menu.dto.request.MenuItemUpdateRequest;
import com.doan.backend.modules.menu.dto.response.MenuItemResponse;
import com.doan.backend.modules.menu.entity.MenuItem;
import com.doan.backend.modules.menu.repository.MenuItemRepository;
import com.doan.backend.modules.menu.service.MenuItemService;
import com.doan.backend.modules.menu.specification.MenuItemSpecification;
import com.doan.backend.modules.restaurant.entity.Restaurant;
import com.doan.backend.modules.restaurant.repository.RestaurantRepository;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MenuItemServiceImpl implements MenuItemService {

    private final MenuItemRepository menuItemRepository;
    private final RestaurantRepository restaurantRepository;

    @Override
    public MenuItemResponse create(MenuItemCreateRequest request) {
        Restaurant restaurant = restaurantRepository.findById(request.getRestaurantId())
                .orElseThrow(() -> new ResourceNotFoundException("Khong tim thay quan an"));
        MenuItem entity = new MenuItem();
        entity.setRestaurant(restaurant);
        applyRequest(entity, request.getName(), request.getPrice(), request.getFlavor(), request.getDescription(),
                request.getImageUrl(), request.getAvailable());
        return mapToResponse(menuItemRepository.save(entity));
    }

    @Override
    public MenuItemResponse update(UUID id, MenuItemUpdateRequest request) {
        MenuItem entity = menuItemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Khong tim thay mon an"));
        applyRequest(entity, request.getName(), request.getPrice(), request.getFlavor(), request.getDescription(),
                request.getImageUrl(), request.getAvailable());
        return mapToResponse(menuItemRepository.save(entity));
    }

    @Override
    public MenuItemResponse getDetail(UUID id) {
        return menuItemRepository.findById(id)
                .map(this::mapToResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Khong tim thay mon an"));
    }

    @Override
    public void delete(UUID id) {
        MenuItem entity = menuItemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Khong tim thay mon an"));
        menuItemRepository.delete(entity);
    }

    @Override
    public PageResponse<MenuItemResponse> search(
            String keyword,
            String flavor,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            int page,
            int size
    ) {
        Page<MenuItem> result = menuItemRepository.findAll(
                MenuItemSpecification.filter(keyword, flavor, minPrice, maxPrice),
                PageRequest.of(page, size));
        List<MenuItemResponse> items = result.getContent().stream().map(this::mapToResponse).toList();
        return PageResponse.from(result, items);
    }

    @Override
    public List<MenuItemResponse> findByRestaurant(UUID restaurantId) {
        return menuItemRepository.findByRestaurantId(restaurantId).stream().map(this::mapToResponse).toList();
    }

    private void applyRequest(
            MenuItem entity,
            String name,
            BigDecimal price,
            String flavor,
            String description,
            String imageUrl,
            Boolean available
    ) {
        entity.setName(name);
        entity.setPrice(price);
        entity.setFlavor(flavor);
        entity.setDescription(description);
        entity.setImageUrl(imageUrl);
        entity.setAvailable(available == null ? Boolean.TRUE : available);
    }

    private MenuItemResponse mapToResponse(MenuItem entity) {
        Restaurant restaurant = entity.getRestaurant();
        return MenuItemResponse.builder()
                .id(entity.getId())
                .restaurantId(restaurant == null ? null : restaurant.getId())
                .restaurantName(restaurant == null ? null : restaurant.getName())
                .name(entity.getName())
                .price(entity.getPrice())
                .flavor(entity.getFlavor())
                .description(entity.getDescription())
                .imageUrl(entity.getImageUrl())
                .available(entity.getAvailable() == null ? Boolean.TRUE : entity.getAvailable())
                .build();
    }
}
