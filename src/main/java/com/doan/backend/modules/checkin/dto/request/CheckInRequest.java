package com.doan.backend.modules.checkin.dto.request;

import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CheckInRequest {

    @NotNull(message = "idQuanAn không được để trống")
    @JsonAlias({"restaurantId", "idquanan", "idCuaHang", "idcuahang"})
    private UUID idQuanAn;

    @NotNull(message = "latitude không được để trống")
    @DecimalMin(value = "-90.0", message = "latitude không hợp lệ")
    @DecimalMax(value = "90.0", message = "latitude không hợp lệ")
    private BigDecimal latitude;

    @NotNull(message = "longitude không được để trống")
    @DecimalMin(value = "-180.0", message = "longitude không hợp lệ")
    @DecimalMax(value = "180.0", message = "longitude không hợp lệ")
    private BigDecimal longitude;
}
