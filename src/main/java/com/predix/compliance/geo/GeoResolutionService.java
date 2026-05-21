package com.predix.compliance.geo;

import com.predix.compliance.metrics.ComplianceMetrics;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class GeoResolutionService {

    private final GeoProvider geoProvider;
    private final ComplianceMetrics metrics;

    public GeoResolutionService(GeoProvider geoProvider, ComplianceMetrics metrics) {
        this.geoProvider = geoProvider;
        this.metrics = metrics;
    }

    public Optional<GeoResolution> resolve(String ip) {
        try {
            return geoProvider.resolve(ip);
        } catch (Exception ex) {
            metrics.incrementGeoLookupFail();
            return Optional.empty();
        }
    }
}
