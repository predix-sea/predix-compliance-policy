package com.predix.compliance.audit;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class IpMaskingUtilTest {

    @Test
    void masksIpv4() {
        assertThat(IpMaskingUtil.mask("203.0.113.1")).isEqualTo("203.0.xxx.xxx");
    }

    @Test
    void hashesIpDeterministically() {
        String h1 = IpMaskingUtil.hash("203.0.113.1");
        String h2 = IpMaskingUtil.hash("203.0.113.1");
        assertThat(h1).isEqualTo(h2);
        assertThat(h1).hasSize(64);
    }
}
