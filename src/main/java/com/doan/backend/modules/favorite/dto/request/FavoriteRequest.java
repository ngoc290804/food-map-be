package com.doan.backend.modules.favorite.dto.request;

import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FavoriteRequest {

    @NotNull(message = "idCuaHang không được để trống")
    @JsonAlias({"restaurantId", "idcuahang"})
    private UUID idCuaHang;
}
