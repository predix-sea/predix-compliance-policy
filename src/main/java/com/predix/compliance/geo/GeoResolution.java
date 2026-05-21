package com.predix.compliance.geo;

import java.util.Optional;

public record GeoResolution(String countryCode, String region) {
    public static Optional<GeoResolution> of(String country, String region) {
        if (country == null || country.isBlank()) {
            return Optional.empty();
        }
        return Optional.of(new GeoResolution(country.toUpperCase(), region));
    }
}
