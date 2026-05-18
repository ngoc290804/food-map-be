package com.doan.backend.modules.restaurant.service;

import com.doan.backend.modules.restaurant.dto.response.GeocodingResult;
import java.util.Optional;

public interface GeocodingService {
    Optional<GeocodingResult> geocode(String address);
}
