package com.predix.compliance.geo;

import com.predix.compliance.config.ComplianceProperties;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;

@Component
public class StaticGeoProvider implements GeoProvider {

    private final Map<String, String> ipCountryMap;

    public StaticGeoProvider(ComplianceProperties properties) {
        this.ipCountryMap = properties.getGeo().getStaticCountryMap();
    }

    @Override
    public Optional<GeoResolution> resolve(String ip) {
        if (ip == null || ip.isBlank()) {
            return Optional.empty();
        }
        String normalized = ip.trim();
        String country = ipCountryMap.get(normalized);
        if (country != null) {
            return GeoResolution.of(country, null);
        }
        if (normalized.startsWith("10.") || normalized.startsWith("192.168.")) {
            return GeoResolution.of("SG", null);
        }
        return Optional.empty();
    }
}
