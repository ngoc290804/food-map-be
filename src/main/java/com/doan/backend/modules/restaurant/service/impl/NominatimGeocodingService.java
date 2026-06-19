package com.doan.backend.modules.restaurant.service.impl;

import com.doan.backend.config.NominatimProperties;
import com.doan.backend.modules.restaurant.dto.response.GeocodingResult;
import com.doan.backend.modules.restaurant.service.GeocodingService;
import com.fasterxml.jackson.databind.JsonNode;
import java.math.BigDecimal;
import java.time.Clock;
import java.util.ArrayList;
import java.util.List;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Slf4j
@Service
public class NominatimGeocodingService implements GeocodingService {

    private final NominatimProperties properties;
    private final RestClient restClient;
    private final Clock clock;
    private long lastRequestAt;

    public NominatimGeocodingService(
            NominatimProperties properties,
            RestClient.Builder restClientBuilder,
            Clock clock
    ) {
        this.properties = properties;
        this.restClient = restClientBuilder.baseUrl(properties.getBaseUrl()).build();
        this.clock = clock;
    }

    @Override
    public Optional<GeocodingResult> geocode(String address) {
        return search(address, 1).stream().findFirst();
    }

    @Override
    public List<GeocodingResult> search(String address, int limit) {
        if (address == null || address.isBlank()) {
            return List.of();
        }

        int safeLimit = Math.max(1, Math.min(limit, 10));
        for (String query : buildQueries(address)) {
            List<GeocodingResult> result = geocodeOne(query, safeLimit);
            if (!result.isEmpty()) {
                log.info("Geocoded address '{}' using query '{}'", address, query);
                return result;
            }
        }

        log.warn("Nominatim did not return coordinates for address '{}'", address);
        return List.of();
    }

    private List<GeocodingResult> geocodeOne(String query, int limit) {
        try {
            throttle();
            JsonNode response = restClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/search")
                            .queryParam("format", "jsonv2")
                            .queryParam("limit", limit)
                            .queryParam("q", query)
                            .queryParam("countrycodes", properties.getCountryCodes())
                            .build())
                    .header("User-Agent", properties.getUserAgent())
                    .header("Accept", "application/json")
                    .retrieve()
                    .body(JsonNode.class);

            if (response == null || !response.isArray() || response.isEmpty()) {
                return List.of();
            }

            List<GeocodingResult> results = new ArrayList<>();
            for (JsonNode item : response) {
                results.add(GeocodingResult.builder()
                        .latitude(new BigDecimal(item.path("lat").asText()))
                        .longitude(new BigDecimal(item.path("lon").asText()))
                        .displayName(item.path("display_name").asText(query))
                        .build());
            }

            return results;
        } catch (RestClientException | IllegalArgumentException ex) {
            log.warn("Cannot geocode query '{}': {}", query, ex.getMessage());
            return List.of();
        }
    }

    private Set<String> buildQueries(String address) {
        String normalizedAddress = address.trim();
        Set<String> queries = new LinkedHashSet<>();
        addQueryVariants(queries, normalizedAddress);

        String withoutHouseNumber = normalizedAddress.replaceFirst("^\\s*\\d+[\\w\\-/]*\\s+", "").trim();
        if (!withoutHouseNumber.equals(normalizedAddress) && !withoutHouseNumber.isBlank()) {
            addQueryVariants(queries, withoutHouseNumber);
        }

        String[] parts = normalizedAddress.split(",");
        if (parts.length > 1) {
            for (int i = 1; i < parts.length; i++) {
                String lessSpecific = joinAddressParts(parts, i);
                if (!lessSpecific.isBlank()) {
                    addQueryVariants(queries, lessSpecific);
                }
            }
        }
        return queries;
    }

    private void addQueryVariants(Set<String> queries, String query) {
        String normalizedQuery = query.trim();
        if (normalizedQuery.isBlank()) {
            return;
        }
        queries.add(normalizedQuery);
        String lowerCaseQuery = normalizedQuery.toLowerCase();
        if (!lowerCaseQuery.contains("việt nam") && !lowerCaseQuery.contains("viet nam")) {
            queries.add(normalizedQuery + ", Việt Nam");
        }
    }

    private String joinAddressParts(String[] parts, int startIndex) {
        StringBuilder builder = new StringBuilder();
        for (int i = startIndex; i < parts.length; i++) {
            String part = parts[i].trim();
            if (part.isBlank()) {
                continue;
            }
            if (!builder.isEmpty()) {
                builder.append(", ");
            }
            builder.append(part);
        }
        return builder.toString();
    }

    private synchronized void throttle() {
        long now = clock.millis();
        long waitMillis = properties.getMinIntervalMillis() - (now - lastRequestAt);
        if (waitMillis > 0) {
            try {
                Thread.sleep(waitMillis);
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
            }
        }
        lastRequestAt = clock.millis();
    }
}
