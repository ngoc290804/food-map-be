package com.doan.backend.modules.review.dto.request;

import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReviewCreateRequest {

    @NotNull(message = "idCuaHang không được để trống")
    @JsonAlias({"restaurantId", "idcuahang"})
    private UUID idCuaHang;

    @JsonAlias({"content", "danhgia"})
    private String danhGia;

    @NotNull(message = "diemDanhGia không được để trống")
    @Min(value = 1, message = "diemDanhGia phải từ 1 đến 5")
    @Max(value = 5, message = "diemDanhGia phải từ 1 đến 5")
    @JsonAlias({"rating", "diemdanhgia"})
    private Integer diemDanhGia;
}
