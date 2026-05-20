package com.doan.backend.modules.review.vo;

import java.math.BigDecimal;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ReviewSummaryVo {
    private final BigDecimal diemDanhGiaTrungBinh;
    private final long soLuongDanhGia;
}
