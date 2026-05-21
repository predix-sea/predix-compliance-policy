package com.predix.compliance.policy;

import com.predix.compliance.domain.PolicyAction;

import java.util.ArrayList;
import java.util.List;

public record PolicyDecision(
        PolicyAction decision,
        String reasonCode,
        String reasonMessage,
        String matchedPolicyCode,
        String matchedRuleCode,
        Integer policyVersion,
        List<String> obligations
) {
    public static PolicyDecision allow(String policyCode, String ruleCode, int version, String message) {
        return new PolicyDecision(PolicyAction.ALLOW, "ALLOW", message, policyCode, ruleCode, version, List.of());
    }

    public static PolicyDecision deny(String reasonCode, String message, String policyCode,
                                      String ruleCode, int version, String... obligations) {
        return new PolicyDecision(PolicyAction.DENY, reasonCode, message, policyCode, ruleCode, version,
                obligations.length == 0 ? List.of() : List.of(obligations));
    }

    public static PolicyDecision review(String reasonCode, String message, String policyCode,
                                        String ruleCode, int version, String... obligations) {
        return new PolicyDecision(PolicyAction.REVIEW, reasonCode, message, policyCode, ruleCode, version,
                List.of(obligations));
    }

    public PolicyDecision withObligations(String obligation) {
        List<String> merged = new ArrayList<>(obligations);
        if (!merged.contains(obligation)) {
            merged.add(obligation);
        }
        return new PolicyDecision(decision, reasonCode, reasonMessage, matchedPolicyCode,
                matchedRuleCode, policyVersion, List.copyOf(merged));
    }
}
