package com.predix.compliance.geo;

import com.predix.compliance.metrics.ComplianceMetrics;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class GeoResolutionServiceTest {

    @Test
    void resolvesViaProvider() {
        GeoProvider provider = ip -> GeoResolution.of("SG", null);
        GeoResolutionService service = new GeoResolutionService(provider, new ComplianceMetrics(new SimpleMeterRegistry()));
        assertThat(service.resolve("1.2.3.4")).map(GeoResolution::countryCode).contains("SG");
    }

    @Test
    void incrementsMetricOnFailure() {
        GeoProvider provider = ip -> { throw new RuntimeException("fail"); };
        ComplianceMetrics metrics = new ComplianceMetrics(new SimpleMeterRegistry());
        GeoResolutionService service = new GeoResolutionService(provider, metrics);
        assertThat(service.resolve("1.2.3.4")).isEmpty();
    }
}
