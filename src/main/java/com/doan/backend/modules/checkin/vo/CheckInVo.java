package com.doan.backend.modules.checkin.vo;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CheckInVo {
    private final UUID id;
    private final UUID idTaiKhoan;
    private final UUID idQuanAn;
    private final String tenQuanAn;
    private final Integer checkin;
    private final Boolean thanhCong;
    private final BigDecimal latitudeHienTai;
    private final BigDecimal longitudeHienTai;
    private final BigDecimal latitudeQuanAn;
    private final BigDecimal longitudeQuanAn;
    private final Double khoangCachMet;
    private final Integer nguongChoPhepMet;
    private final LocalDateTime ngayTao;
}
