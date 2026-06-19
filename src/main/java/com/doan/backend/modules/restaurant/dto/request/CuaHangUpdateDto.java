package com.doan.backend.modules.restaurant.dto.request;

import com.doan.backend.common.enums.MenuCategory;
import com.doan.backend.common.enums.MenuDetail;
import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
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

    private String imagePublicId;

    @DecimalMin(value = "-90.0", message = "latitude khong hop le")
    @DecimalMax(value = "90.0", message = "latitude khong hop le")
    private BigDecimal latitude;

    @DecimalMin(value = "-180.0", message = "longitude khong hop le")
    @DecimalMax(value = "180.0", message = "longitude khong hop le")
    private BigDecimal longitude;

    @NotNull(message = "loaiCuaHang khong duoc de trong")
    private MenuCategory loaiCuaHang;

    private MenuDetail loaiKinhDoanh;

    @JsonAlias("status")
    private String trangThai;
}
