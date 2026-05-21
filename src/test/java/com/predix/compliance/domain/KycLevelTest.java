package com.predix.compliance.domain;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class KycLevelTest {

    @Test
    void satisfiesHierarchy() {
        assertThat(KycLevel.FULL.satisfies(KycLevel.BASIC)).isTrue();
        assertThat(KycLevel.BASIC.satisfies(KycLevel.FULL)).isFalse();
        assertThat(KycLevel.fromString("full")).isEqualTo(KycLevel.FULL);
    }
}
