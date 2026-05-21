package com.predix.compliance.policy;

import com.predix.compliance.domain.PolicyRuleEntity;
import com.predix.compliance.domain.PolicySetEntity;

import java.util.List;

public record LoadedPolicySet(
        PolicySetEntity policySet,
        List<PolicyRuleEntity> rules
) {
    public String policyCode() {
        return policySet.getPolicyCode();
    }

    public int version() {
        return policySet.getVersion();
    }
}
