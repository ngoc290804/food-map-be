package com.doan.backend.modules.favorite.vo;

import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class FavoriteVo {
    private final UUID id;
    private final UUID idCuaHang;
    private final UUID idTaiKhoan;
    private final String tenQuanAn;
    private final String diaChi;
    private final String hinhAnh;
    private final LocalDateTime ngayTao;
    private final LocalDateTime ngaySua;
}
