package com.predix.compliance.geo;

import com.predix.compliance.config.ComplianceProperties;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class StaticGeoProviderTest {

    @Test
    void resolvesConfiguredIp() {
        ComplianceProperties props = new ComplianceProperties();
        ComplianceProperties.GeoProperties geo = new ComplianceProperties.GeoProperties();
        geo.setStaticCountryMap(Map.of("203.0.113.99", "CN"));
        props.setGeo(geo);
        StaticGeoProvider provider = new StaticGeoProvider(props);
        assertThat(provider.resolve("203.0.113.99")).isPresent()
                .get()
                .extracting(GeoResolution::countryCode)
                .isEqualTo("CN");
    }
}
