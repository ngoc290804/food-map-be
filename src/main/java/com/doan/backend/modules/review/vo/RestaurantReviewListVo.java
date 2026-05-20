package com.doan.backend.modules.review.vo;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class RestaurantReviewListVo {
    private final UUID idCuaHang;
    private final BigDecimal diemDanhGiaTrungBinh;
    private final long soLuongDanhGia;
    private final int page;
    private final int size;
    private final long totalElements;
    private final int totalPages;
    private final List<ReviewVo> danhSachDanhGia;
}
