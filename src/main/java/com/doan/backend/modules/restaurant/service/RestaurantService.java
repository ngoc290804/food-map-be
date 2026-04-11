package com.doan.backend.modules.restaurant.service;

import com.doan.backend.common.dto.PageResponse;
import com.doan.backend.common.enums.MenuCategory;
import com.doan.backend.common.enums.MenuDetail;
import com.doan.backend.modules.restaurant.dto.request.CuaHangCreateDto;
import com.doan.backend.modules.restaurant.dto.request.CuaHangUpdateDto;
import com.doan.backend.modules.restaurant.vo.CuaHangVo;
import java.util.UUID;

public interface RestaurantService {
    CuaHangVo create(CuaHangCreateDto request);

    CuaHangVo update(UUID id, CuaHangUpdateDto request);

    CuaHangVo getDetail(UUID id);

    void delete(UUID id);

    PageResponse<CuaHangVo> search(String keyword, int page, int size);

    PageResponse<CuaHangVo> search(
            String keyword,
            MenuCategory loaiCuaHang,
            MenuDetail loaiKinhDoanh,
            int page,
            int size
    );
}
