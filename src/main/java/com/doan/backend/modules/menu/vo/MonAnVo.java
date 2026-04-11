package com.doan.backend.modules.menu.vo;

import java.math.BigDecimal;
import java.util.UUID;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class MonAnVo {
    private final UUID id;
    private final UUID idCuaHang;
    private final String tenCuaHang;
    private final String tenMonAn;
    private final BigDecimal giaTien;
    private final String nguyenLieuChinh;
    private final String moTa;
    private final String hinhAnh;
    private final Boolean conBan;
}
