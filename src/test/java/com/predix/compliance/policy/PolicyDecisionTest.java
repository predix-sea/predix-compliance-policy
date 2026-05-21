package com.predix.compliance.policy;

import com.predix.compliance.domain.PolicyAction;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PolicyDecisionTest {

    @Test
    void buildsAllowDenyReview() {
        assertThat(PolicyDecision.allow("P", "R", 1, "ok").decision()).isEqualTo(PolicyAction.ALLOW);
        assertThat(PolicyDecision.deny("C", "msg", "P", "R", 1).decision()).isEqualTo(PolicyAction.DENY);
        assertThat(PolicyDecision.review("C", "msg", "P", "R", 1, "MANUAL_REVIEW").obligations())
                .contains("MANUAL_REVIEW");
    }

    @Test
    void mergesObligations() {
        PolicyDecision base = PolicyDecision.deny("C", "m", "P", "R", 1);
        PolicyDecision merged = base.withObligations("KYC_REQUIRED");
        assertThat(merged.obligations()).contains("KYC_REQUIRED");
    }
}
