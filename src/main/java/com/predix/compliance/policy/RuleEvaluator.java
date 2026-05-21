package com.predix.compliance.policy;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.predix.compliance.domain.*;
import com.predix.compliance.repository.CountryProfileRepository;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class RuleEvaluator {

    private final CountryProfileRepository countryProfileRepository;
    private final ObjectMapper objectMapper;

    public RuleEvaluator(CountryProfileRepository countryProfileRepository, ObjectMapper objectMapper) {
        this.countryProfileRepository = countryProfileRepository;
        this.objectMapper = objectMapper;
    }

    public Optional<PolicyDecision> evaluate(PolicyRuleEntity rule, LoadedPolicySet policySet, EvaluationContext ctx) {
        if (!rule.isEnabled()) {
            return Optional.empty();
        }
        return switch (rule.getRuleType()) {
            case GEO_BLOCK -> evaluateGeoBlock(rule, policySet, ctx);
            case ACTION_DENYLIST -> evaluateDenylist(rule, policySet, ctx);
            case KYC_REQUIRED -> evaluateKyc(rule, policySet, ctx);
            case COUNTRY_TIER -> evaluateCountryTier(rule, policySet, ctx);
            case ACTION_ALLOWLIST -> evaluateAllowlist(rule, policySet, ctx);
        };
    }

    @SuppressWarnings("unchecked")
    private Optional<PolicyDecision> evaluateGeoBlock(PolicyRuleEntity rule, LoadedPolicySet policySet,
                                                      EvaluationContext ctx) {
        Map<String, Object> cond = rule.getConditionsJson();
        boolean mainland = Boolean.TRUE.equals(cond.get("mainlandChina"));
        List<String> blocked = cond.containsKey("blockedCountries")
                ? (List<String>) cond.get("blockedCountries") : List.of("CN");

        boolean blockedHit = false;
        if (mainland && ctx.isMainlandChina()) {
            blockedHit = true;
        } else if (ctx.countryCode() != null) {
            blockedHit = blocked.stream().anyMatch(c -> c.equalsIgnoreCase(ctx.countryCode()));
        }

        if (!blockedHit) {
            return Optional.empty();
        }
        if (rule.getAction() == PolicyAction.DENY) {
            return Optional.of(PolicyDecision.deny(
                    ErrorCodes.COMPLIANCE_CN_BLOCKED,
                    render(rule.getReasonTemplate(), ctx),
                    policySet.policyCode(), rule.getRuleCode(), policySet.version()));
        }
        return Optional.empty();
    }

    @SuppressWarnings("unchecked")
    private Optional<PolicyDecision> evaluateDenylist(PolicyRuleEntity rule, LoadedPolicySet policySet,
                                                      EvaluationContext ctx) {
        Map<String, Object> cond = rule.getConditionsJson();
        List<String> denied = cond.containsKey("deniedWallets")
                ? (List<String>) cond.get("deniedWallets") : List.of();
        if (ctx.walletAddress() == null) {
            return Optional.empty();
        }
        boolean hit = denied.stream().anyMatch(w -> w.equalsIgnoreCase(ctx.walletAddress()));
        if (!hit) {
            return Optional.empty();
        }
        return Optional.of(applyAction(rule, policySet, ctx, "WALLET_DENIED"));
    }

    @SuppressWarnings("unchecked")
    private Optional<PolicyDecision> evaluateKyc(PolicyRuleEntity rule, LoadedPolicySet policySet,
                                                 EvaluationContext ctx) {
        Map<String, Object> cond = rule.getConditionsJson();
        List<String> actions = (List<String>) cond.getOrDefault("actions", List.of());
        if (!actions.contains(ctx.actionType().name())) {
            return Optional.empty();
        }
        KycLevel required = KycLevel.fromString(String.valueOf(cond.get("minKycLevel")));
        if (ctx.kycLevel().satisfies(required)) {
            return Optional.empty();
        }
        return Optional.of(applyAction(rule, policySet, ctx, ErrorCodes.KYC_LEVEL_INSUFFICIENT)
                .withObligations("KYC_REQUIRED"));
    }

    private Optional<PolicyDecision> evaluateCountryTier(PolicyRuleEntity rule, LoadedPolicySet policySet,
                                                         EvaluationContext ctx) {
        if (ctx.countryCode() == null || ctx.countryCode().isBlank()) {
            return Optional.empty();
        }
        return countryProfileRepository.findById(ctx.countryCode().toUpperCase())
                .flatMap(profile -> evaluateProfile(rule, policySet, ctx, profile));
    }

    private Optional<PolicyDecision> evaluateProfile(PolicyRuleEntity rule, LoadedPolicySet policySet,
                                                     EvaluationContext ctx, CountryProfileEntity profile) {
        if (!profile.isAllowed()) {
            return Optional.of(applyAction(rule, policySet, ctx, "COUNTRY_NOT_ALLOWED"));
        }
        ComplianceActionType action = ctx.actionType();
        if (action.isTradingAction() && !profile.isTradeAllowed()) {
            return Optional.of(applyAction(rule, policySet, ctx, "TRADE_NOT_ALLOWED"));
        }
        if (action.isFundAction() && !profile.isCustodyAllowed()) {
            return Optional.of(applyAction(rule, policySet, ctx, "CUSTODY_NOT_ALLOWED"));
        }
        return Optional.empty();
    }

    @SuppressWarnings("unchecked")
    private Optional<PolicyDecision> evaluateAllowlist(PolicyRuleEntity rule, LoadedPolicySet policySet,
                                                       EvaluationContext ctx) {
        Map<String, Object> cond = rule.getConditionsJson();
        List<String> actions = (List<String>) cond.getOrDefault("actions", List.of());
        if (!actions.contains(ctx.actionType().name())) {
            return Optional.empty();
        }
        if (rule.getAction() == PolicyAction.ALLOW) {
            return Optional.of(PolicyDecision.allow(policySet.policyCode(), rule.getRuleCode(),
                    policySet.version(), render(rule.getReasonTemplate(), ctx)));
        }
        return Optional.empty();
    }

    private PolicyDecision applyAction(PolicyRuleEntity rule, LoadedPolicySet policySet,
                                       EvaluationContext ctx, String reasonCode) {
        String message = render(rule.getReasonTemplate(), ctx);
        return switch (rule.getAction()) {
            case ALLOW -> PolicyDecision.allow(policySet.policyCode(), rule.getRuleCode(), policySet.version(), message);
            case REVIEW -> PolicyDecision.review(reasonCode, message, policySet.policyCode(),
                    rule.getRuleCode(), policySet.version(), "MANUAL_REVIEW");
            case DENY -> PolicyDecision.deny(reasonCode, message, policySet.policyCode(),
                    rule.getRuleCode(), policySet.version());
        };
    }

    private String render(String template, EvaluationContext ctx) {
        if (template == null) {
            return "";
        }
        return template
                .replace("{{countryCode}}", nullToEmpty(ctx.countryCode()))
                .replace("{{actionType}}", ctx.actionType().name())
                .replace("{{kycLevel}}", ctx.kycLevel().name())
                .replace("{{region}}", nullToEmpty(ctx.region()));
    }

    private String nullToEmpty(String v) {
        return v == null ? "" : v;
    }

    public static final class ErrorCodes {
        public static final String COMPLIANCE_CN_BLOCKED = "COMPLIANCE_CN_BLOCKED";
        public static final String KYC_LEVEL_INSUFFICIENT = "KYC_LEVEL_INSUFFICIENT";

        private ErrorCodes() {}
    }
}
