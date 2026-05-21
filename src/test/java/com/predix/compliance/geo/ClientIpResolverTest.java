package com.predix.compliance.geo;

import com.predix.compliance.config.ComplianceProperties;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ClientIpResolverTest {

    @Test
    void usesForwardedIpWhenProxyTrusted() {
        ComplianceProperties props = new ComplianceProperties();
        props.setTrustedProxies(List.of("10.0.0.1"));
        ClientIpResolver resolver = new ClientIpResolver(props);
        String ip = resolver.resolveFromHeaders("10.0.0.1", "203.0.113.50, 10.0.0.2");
        assertThat(ip).isEqualTo("203.0.113.50");
    }

    @Test
    void trustsLoopbackAsProxy() {
        ComplianceProperties props = new ComplianceProperties();
        ClientIpResolver resolver = new ClientIpResolver(props);
        String ip = resolver.resolveFromHeaders("127.0.0.1", "203.0.113.10");
        assertThat(ip).isEqualTo("203.0.113.10");
    }

    @Test
    void matchesCidrTrustedProxy() {
        ComplianceProperties props = new ComplianceProperties();
        props.setTrustedProxies(List.of("10.0.0.0/8"));
        ClientIpResolver resolver = new ClientIpResolver(props);
        String ip = resolver.resolveFromHeaders("10.0.1.2", "203.0.113.20");
        assertThat(ip).isEqualTo("203.0.113.20");
    }

    @Test
    void ignoresForwardedIpWhenProxyUntrusted() {
        ComplianceProperties props = new ComplianceProperties();
        props.setTrustedProxies(List.of("10.0.0.1"));
        ClientIpResolver resolver = new ClientIpResolver(props);
        String ip = resolver.resolveFromHeaders("203.0.113.9", "1.2.3.4");
        assertThat(ip).isEqualTo("203.0.113.9");
    }
}
