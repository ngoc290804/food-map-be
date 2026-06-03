package com.doan.backend.modules.restaurant.service;

import com.doan.backend.common.dto.PageResponse;
import com.doan.backend.common.enums.MenuCategory;
import com.doan.backend.common.enums.MenuDetail;
import com.doan.backend.modules.restaurant.dto.request.CuaHangCreateDto;
import com.doan.backend.modules.restaurant.dto.request.CuaHangUpdateDto;
import com.doan.backend.modules.restaurant.vo.CuaHangVo;
import java.util.List;
import java.util.UUID;
import org.springframework.web.multipart.MultipartFile;

public interface RestaurantService {
    CuaHangVo create(CuaHangCreateDto request);

    CuaHangVo create(CuaHangCreateDto request, MultipartFile image);

    CuaHangVo update(UUID id, CuaHangUpdateDto request);

    CuaHangVo update(UUID id, CuaHangUpdateDto request, MultipartFile image);

    CuaHangVo getDetail(UUID id);

    CuaHangVo getByOwnerId(UUID idTaiKhoan);

    CuaHangVo getMyRestaurant();

    void delete(UUID id);

    PageResponse<CuaHangVo> search(String keyword, int page, int size);

    PageResponse<CuaHangVo> ranking(int page, int size);

    PageResponse<CuaHangVo> search(
            String keyword,
            MenuCategory loaiCuaHang,
            MenuDetail loaiKinhDoanh,
            int page,
            int size
    );

    List<CuaHangVo> findActiveForChatbot(int limit);
}
