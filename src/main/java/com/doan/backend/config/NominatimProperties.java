package com.doan.backend.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "app.nominatim")
public class NominatimProperties {
    private String baseUrl = "https://nominatim.openstreetmap.org";
    private String userAgent = "food-map-be/1.0";
    private String countryCodes = "vn";
    private long minIntervalMillis = 1000;
}
