package com.predix.compliance.cache;

import com.predix.compliance.domain.PolicyAction;
import com.predix.compliance.domain.PolicyRuleEntity;
import com.predix.compliance.domain.PolicySetEntity;
import com.predix.compliance.domain.RuleType;
import com.predix.compliance.policy.LoadedPolicySet;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public record PolicySnapshot(
        String policyCode,
        int version,
        int priority,
        List<RuleSnapshot> rules
) {
    public record RuleSnapshot(
            Long id,
            Long policySetId,
            String ruleCode,
            RuleType ruleType,
            Map<String, Object> conditionsJson,
            PolicyAction action,
            String reasonTemplate,
            boolean enabled
    ) {}

    public static PolicySnapshot from(LoadedPolicySet loaded) {
        PolicySetEntity set = loaded.policySet();
        List<RuleSnapshot> rules = loaded.rules().stream()
                .map(PolicySnapshot::fromRule)
                .collect(Collectors.toList());
        return new PolicySnapshot(set.getPolicyCode(), set.getVersion(), set.getPriority(), rules);
    }

    private static RuleSnapshot fromRule(PolicyRuleEntity r) {
        return new RuleSnapshot(r.getId(), r.getPolicySetId(), r.getRuleCode(), r.getRuleType(),
                r.getConditionsJson(), r.getAction(), r.getReasonTemplate(), r.isEnabled());
    }

    public LoadedPolicySet toLoaded() {
        PolicySetEntity set = PolicySetEntity.builder()
                .policyCode(policyCode)
                .version(version)
                .priority(priority)
                .build();
        List<PolicyRuleEntity> ruleEntities = rules.stream().map(r -> PolicyRuleEntity.builder()
                .id(r.id())
                .policySetId(r.policySetId())
                .ruleCode(r.ruleCode())
                .ruleType(r.ruleType())
                .conditionsJson(r.conditionsJson())
                .action(r.action())
                .reasonTemplate(r.reasonTemplate())
                .enabled(r.enabled())
                .build()).collect(Collectors.toList());
        return new LoadedPolicySet(set, ruleEntities);
    }
}
