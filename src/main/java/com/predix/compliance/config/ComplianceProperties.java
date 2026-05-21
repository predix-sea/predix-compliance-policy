package com.predix.compliance.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;
import java.util.List;
import java.util.Map;

@ConfigurationProperties(prefix = "predix.compliance")
public class ComplianceProperties {

    private String adminToken = "changeme";
    private List<String> trustedProxies = List.of("127.0.0.1");
    private String defaultStrategy = "FAIL_CLOSED";
    private Duration cacheTtl = Duration.ofMinutes(5);
    private GeoProperties geo = new GeoProperties();
    private List<String> criticalActions = List.of("PLACE_ORDER", "DEPOSIT", "WITHDRAW");

    public String getAdminToken() {
        return adminToken;
    }

    public void setAdminToken(String adminToken) {
        this.adminToken = adminToken;
    }

    public List<String> getTrustedProxies() {
        return trustedProxies;
    }

    public void setTrustedProxies(List<String> trustedProxies) {
        this.trustedProxies = trustedProxies;
    }

    public boolean isFailClosed() {
        return "FAIL_CLOSED".equalsIgnoreCase(defaultStrategy);
    }

    public String getDefaultStrategy() {
        return defaultStrategy;
    }

    public void setDefaultStrategy(String defaultStrategy) {
        this.defaultStrategy = defaultStrategy;
    }

    public Duration getCacheTtl() {
        return cacheTtl;
    }

    public void setCacheTtl(Duration cacheTtl) {
        this.cacheTtl = cacheTtl;
    }

    public GeoProperties getGeo() {
        return geo;
    }

    public void setGeo(GeoProperties geo) {
        this.geo = geo;
    }

    public List<String> getCriticalActions() {
        return criticalActions;
    }

    public void setCriticalActions(List<String> criticalActions) {
        this.criticalActions = criticalActions;
    }

    public static class GeoProperties {
        private String provider = "static";
        private Map<String, String> staticCountryMap = Map.of();

        public String getProvider() {
            return provider;
        }

        public void setProvider(String provider) {
            this.provider = provider;
        }

        public Map<String, String> getStaticCountryMap() {
            return staticCountryMap;
        }

        public void setStaticCountryMap(Map<String, String> staticCountryMap) {
            this.staticCountryMap = staticCountryMap;
        }
    }
}
