package com.predix.compliance.policy;

import com.predix.compliance.domain.ComplianceActionType;
import com.predix.compliance.domain.KycLevel;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class EvaluationContextTest {

    @Test
    void detectsMainlandChinaByCountryAndRegion() {
        EvaluationContext byCountry = new EvaluationContext("r", "u", null, "1.1.1.1", "CN", null,
                ComplianceActionType.LOGIN, KycLevel.NONE, List.of());
        EvaluationContext byRegion = new EvaluationContext("r", "u", null, "1.1.1.1", "HK", "CN-HK",
                ComplianceActionType.LOGIN, KycLevel.NONE, List.of());
        assertThat(byCountry.isMainlandChina()).isTrue();
        assertThat(byRegion.isMainlandChina()).isTrue();
    }

    @Test
    void subjectIdPrefersUserId() {
        EvaluationContext ctx = new EvaluationContext("r", "user-1", "0xwallet", "1.1.1.1", "SG", null,
                ComplianceActionType.LOGIN, KycLevel.NONE, List.of());
        assertThat(ctx.subjectId()).isEqualTo("user-1");
    }
}
