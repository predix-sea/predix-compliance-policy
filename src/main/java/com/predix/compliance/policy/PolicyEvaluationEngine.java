package com.predix.compliance.policy;

import com.predix.compliance.config.ComplianceProperties;
import com.predix.compliance.domain.ComplianceActionType;
import com.predix.compliance.domain.PolicyAction;
import com.predix.compliance.domain.PolicyRuleEntity;
import com.predix.compliance.domain.RuleType;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Component
public class PolicyEvaluationEngine {

    private static final Map<RuleType, Integer> RULE_PRIORITY = new EnumMap<>(RuleType.class);

    static {
        RULE_PRIORITY.put(RuleType.GEO_BLOCK, 1);
        RULE_PRIORITY.put(RuleType.ACTION_DENYLIST, 2);
        RULE_PRIORITY.put(RuleType.KYC_REQUIRED, 3);
        RULE_PRIORITY.put(RuleType.COUNTRY_TIER, 4);
        RULE_PRIORITY.put(RuleType.ACTION_ALLOWLIST, 5);
    }

    private final RuleEvaluator ruleEvaluator;
    private final ComplianceProperties properties;

    public PolicyEvaluationEngine(RuleEvaluator ruleEvaluator, ComplianceProperties properties) {
        this.ruleEvaluator = ruleEvaluator;
        this.properties = properties;
    }

    public PolicyDecision evaluate(LoadedPolicySet policySet, EvaluationContext ctx) {
        List<PolicyRuleEntity> ordered = policySet.rules().stream()
                .sorted(Comparator.comparingInt(r -> RULE_PRIORITY.getOrDefault(r.getRuleType(), 99)))
                .toList();

        PolicyDecision allowCandidate = null;
        for (PolicyRuleEntity rule : ordered) {
            Optional<PolicyDecision> result = ruleEvaluator.evaluate(rule, policySet, ctx);
            if (result.isEmpty()) {
                continue;
            }
            PolicyDecision decision = result.get();
            if (decision.decision() == PolicyAction.DENY || decision.decision() == PolicyAction.REVIEW) {
                return decision;
            }
            if (decision.decision() == PolicyAction.ALLOW) {
                allowCandidate = decision;
            }
        }
        if (allowCandidate != null) {
            return allowCandidate;
        }
        return defaultDecision(policySet, ctx);
    }

    private PolicyDecision defaultDecision(LoadedPolicySet policySet, EvaluationContext ctx) {
        boolean critical = properties.getCriticalActions().contains(ctx.actionType().name());
        boolean missingCountry = ctx.countryCode() == null || ctx.countryCode().isBlank();

        if (properties.isFailClosed() && critical && missingCountry) {
            return PolicyDecision.deny(
                    "DEFAULT_FAIL_CLOSED",
                    "Critical action " + ctx.actionType() + " requires resolvable country context",
                    policySet.policyCode(),
                    "DEFAULT",
                    policySet.version());
        }
        return PolicyDecision.allow(policySet.policyCode(), "IMPLICIT_ALLOW", policySet.version(),
                "No deny rule matched; implicit allow");
    }
}
