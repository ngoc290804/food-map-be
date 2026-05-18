package com.doan.backend.modules.menu.dto.request;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class MonAnCreateDto {

    @JsonAlias("restaurantId")
    @NotNull(message = "idCuaHang khong duoc de trong")
    private UUID idCuaHang;

    @JsonAlias("name")
    @NotBlank(message = "tenMonAn khong duoc de trong")
    private String tenMonAn;

    @JsonAlias("price")
    @NotNull(message = "giaTien khong duoc de trong")
    @DecimalMin(value = "0.0", inclusive = false, message = "giaTien phai lon hon 0")
    private BigDecimal giaTien;

    @JsonAlias({"mainIngredient", "flavor"})
    private String nguyenLieuChinh;

    @JsonAlias("description")
    private String moTa;

    @JsonAlias("imageUrl")
    private String hinhAnh;

    private String imagePublicId;

    @JsonAlias("available")
    private Boolean conBan;
}
