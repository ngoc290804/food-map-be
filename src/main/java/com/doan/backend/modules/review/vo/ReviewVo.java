package com.doan.backend.modules.review.vo;

import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ReviewVo {
    private final UUID id;
    private final UUID idTaiKhoan;
    private final UUID idCuaHang;
    private final String tenTaiKhoan;
    private final String tenQuanAn;
    private final String danhGia;
    private final Integer diemDanhGia;
    private final LocalDateTime ngayTao;
}
