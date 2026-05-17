package com.doan.backend.modules.menu.service.impl;

import com.doan.backend.common.dto.PageResponse;
import com.doan.backend.common.exception.ResourceNotFoundException;
import com.doan.backend.modules.menu.dto.request.MonAnCreateDto;
import com.doan.backend.modules.menu.dto.request.MonAnUpdateDto;
import com.doan.backend.modules.menu.entity.MenuItem;
import com.doan.backend.modules.menu.repository.MenuItemRepository;
import com.doan.backend.modules.menu.service.MenuItemService;
import com.doan.backend.modules.menu.specification.MenuItemSpecification;
import com.doan.backend.modules.menu.vo.MonAnVo;
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
    public MonAnVo create(MonAnCreateDto request) {
        return create(request.getIdCuaHang(), request);
    }

    @Override
    public MonAnVo create(UUID restaurantId, MonAnCreateDto request) {
        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new ResourceNotFoundException("Khong tim thay quan an"));
        MenuItem entity = new MenuItem();
        entity.setRestaurant(restaurant);
        applyRequest(entity, request.getTenMonAn(), request.getGiaTien(), request.getNguyenLieuChinh(), request.getMoTa(),
                request.getHinhAnh(), request.getImagePublicId(), request.getConBan());
        return mapToResponse(menuItemRepository.save(entity));
    }

    @Override
    public MonAnVo update(UUID id, MonAnUpdateDto request) {
        MenuItem entity = menuItemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Khong tim thay mon an"));
        Restaurant restaurant = restaurantRepository.findById(request.getIdCuaHang())
                .orElseThrow(() -> new ResourceNotFoundException("Khong tim thay quan an"));
        entity.setRestaurant(restaurant);
        applyRequest(entity, request.getTenMonAn(), request.getGiaTien(), request.getNguyenLieuChinh(), request.getMoTa(),
                request.getHinhAnh(), request.getImagePublicId(), request.getConBan());
        return mapToResponse(menuItemRepository.save(entity));
    }

    @Override
    public MonAnVo getDetail(UUID id) {
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
    public PageResponse<MonAnVo> search(
            String keyword,
            String nguyenLieuChinh,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            int page,
            int size
    ) {
        Page<MenuItem> result = menuItemRepository.findAll(
                MenuItemSpecification.filter(keyword, nguyenLieuChinh, minPrice, maxPrice),
                PageRequest.of(page, size));
        List<MonAnVo> items = result.getContent().stream().map(this::mapToResponse).toList();
        return PageResponse.from(result, items);
    }

    @Override
    public List<MonAnVo> findByRestaurant(UUID restaurantId) {
        return menuItemRepository.findByRestaurantId(restaurantId).stream().map(this::mapToResponse).toList();
    }

    private void applyRequest(
            MenuItem entity,
            String name,
            BigDecimal price,
            String mainIngredient,
            String description,
            String imageUrl,
            String imagePublicId,
            Boolean available
    ) {
        entity.setName(name);
        entity.setPrice(price);
        entity.setFlavor(mainIngredient);
        entity.setDescription(description);
        entity.setImageUrl(imageUrl);
        entity.setImagePublicId(imagePublicId);
        entity.setAvailable(available == null ? Boolean.TRUE : available);
    }

    private MonAnVo mapToResponse(MenuItem entity) {
        Restaurant restaurant = entity.getRestaurant();
        return MonAnVo.builder()
                .id(entity.getId())
                .idCuaHang(restaurant == null ? null : restaurant.getId())
                .tenCuaHang(restaurant == null ? null : restaurant.getName())
                .tenMonAn(entity.getName())
                .giaTien(entity.getPrice())
                .nguyenLieuChinh(entity.getFlavor())
                .moTa(entity.getDescription())
                .hinhAnh(entity.getImageUrl())
                .imagePublicId(entity.getImagePublicId())
                .conBan(entity.getAvailable() == null ? Boolean.TRUE : entity.getAvailable())
                .build();
    }
}
