package com.doan.backend.modules.restaurant.dto.request;

import com.doan.backend.common.enums.MenuCategory;
import com.doan.backend.common.enums.MenuDetail;
import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CuaHangUpdateDto {

    @JsonAlias("name")
    @NotBlank(message = "tenQuanAn khong duoc de trong")
    @Size(max = 255)
    private String tenQuanAn;

    @JsonAlias("address")
    @NotBlank(message = "diaChi khong duoc de trong")
    @Size(max = 500)
    private String diaChi;

    @JsonAlias("openTime")
    private String gioMoCua;

    @JsonAlias("closeTime")
    private String gioDongCua;

    @JsonAlias("description")
    @Size(max = 1000)
    private String moTa;

    @JsonAlias("imageUrl")
    private String hinhAnh;

    @NotNull(message = "loaiCuaHang khong duoc de trong")
    private MenuCategory loaiCuaHang;

    private MenuDetail loaiKinhDoanh;

    @JsonAlias("status")
    private String trangThai;
}
