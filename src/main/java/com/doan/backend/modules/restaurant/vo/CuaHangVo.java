package com.doan.backend.modules.restaurant.vo;

import java.util.UUID;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CuaHangVo {
    private final UUID id;
    private final String tenQuanAn;
    private final String diaChi;
    private final String gioMoCua;
    private final String gioDongCua;
    private final String moTa;
    private final String hinhAnh;
    private final String trangThai;
    private final String loaiCuaHang;
    private final String loaiKinhDoanh;
}
