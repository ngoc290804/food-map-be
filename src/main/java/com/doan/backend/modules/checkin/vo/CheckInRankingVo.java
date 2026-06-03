package com.doan.backend.modules.checkin.vo;

import lombok.Getter;

@Getter
public class CheckInRankingVo {
    private final String tenTaiKhoan;
    private final String hoTen;
    private final long soLanCheckinThanhCong;

    public CheckInRankingVo(String tenTaiKhoan, String hoTen, long soLanCheckinThanhCong) {
        this.tenTaiKhoan = tenTaiKhoan;
        this.hoTen = hoTen;
        this.soLanCheckinThanhCong = soLanCheckinThanhCong;
    }
}
