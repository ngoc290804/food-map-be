package com.doan.backend.modules.restaurant.dto.response;

import java.math.BigDecimal;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class GeocodingResult {
    private final BigDecimal latitude;
    private final BigDecimal longitude;
    private final String displayName;
}
