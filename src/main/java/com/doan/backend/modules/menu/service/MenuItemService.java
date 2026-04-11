package com.doan.backend.modules.menu.service;

import com.doan.backend.common.dto.PageResponse;
import com.doan.backend.modules.menu.dto.request.MonAnCreateDto;
import com.doan.backend.modules.menu.dto.request.MonAnUpdateDto;
import com.doan.backend.modules.menu.vo.MonAnVo;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public interface MenuItemService {
    MonAnVo create(MonAnCreateDto request);

    MonAnVo create(UUID restaurantId, MonAnCreateDto request);

    MonAnVo update(UUID id, MonAnUpdateDto request);

    MonAnVo getDetail(UUID id);

    void delete(UUID id);

    PageResponse<MonAnVo> search(String keyword, String nguyenLieuChinh, BigDecimal minPrice, BigDecimal maxPrice,
                                 int page, int size);

    List<MonAnVo> findByRestaurant(UUID restaurantId);
}
