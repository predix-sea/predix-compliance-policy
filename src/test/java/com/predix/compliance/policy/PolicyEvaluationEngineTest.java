package com.predix.compliance.policy;

import com.predix.compliance.config.ComplianceProperties;
import com.predix.compliance.domain.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class PolicyEvaluationEngineTest {

    private PolicyEvaluationEngine engine;
    private LoadedPolicySet policySet;
    private StubCountryProfileRepository countryProfileRepository;

    @BeforeEach
    void setUp() {
        ComplianceProperties props = new ComplianceProperties();
        props.setCriticalActions(List.of("PLACE_ORDER", "DEPOSIT", "WITHDRAW"));
        countryProfileRepository = new StubCountryProfileRepository();
        RuleEvaluator ruleEvaluator = new RuleEvaluator(countryProfileRepository, new ObjectMapper());
        engine = new PolicyEvaluationEngine(ruleEvaluator, props);

        PolicySetEntity set = PolicySetEntity.builder()
                .policyCode("GLOBAL_COMPLIANCE")
                .version(1)
                .priority(10)
                .build();
        policySet = new LoadedPolicySet(set, List.of(
                geoBlockRule(),
                kycTradingRule(),
                kycViewRule(),
                countryTierRule(),
                defaultAllowRule()
        ));
    }

    @Test
    void blocksMainlandChinaWithHighestPriority() {
        EvaluationContext ctx = context("CN", null, ComplianceActionType.LOGIN, KycLevel.FULL);
        PolicyDecision decision = engine.evaluate(policySet, ctx);
        assertThat(decision.decision()).isEqualTo(PolicyAction.DENY);
        assertThat(decision.reasonCode()).isEqualTo(RuleEvaluator.ErrorCodes.COMPLIANCE_CN_BLOCKED);
        assertThat(decision.matchedRuleCode()).isEqualTo("CN_MAINLAND_BLOCK");
    }

    @Test
    void deniesTradingWhenKycInsufficient() {
        countryProfileRepository.put(sgProfile());
        EvaluationContext ctx = context("SG", null, ComplianceActionType.PLACE_ORDER, KycLevel.BASIC);
        PolicyDecision decision = engine.evaluate(policySet, ctx);
        assertThat(decision.decision()).isEqualTo(PolicyAction.DENY);
        assertThat(decision.reasonCode()).isEqualTo(RuleEvaluator.ErrorCodes.KYC_LEVEL_INSUFFICIENT);
        assertThat(decision.obligations()).contains("KYC_REQUIRED");
    }

    @Test
    void deniesViewMarketWhenKycNone() {
        EvaluationContext ctx = context("SG", null, ComplianceActionType.VIEW_MARKET, KycLevel.NONE);
        PolicyDecision decision = engine.evaluate(policySet, ctx);
        assertThat(decision.decision()).isEqualTo(PolicyAction.DENY);
        assertThat(decision.matchedRuleCode()).isEqualTo("KYC_VIEW_GATE");
    }

    @Test
    void blocksIndonesiaTradingViaCountryTier() {
        countryProfileRepository.put(CountryProfileEntity.builder().countryCode("ID").tier("T3")
                .allowed(true).tradeAllowed(false).custodyAllowed(false).build());
        EvaluationContext ctx = context("ID", null, ComplianceActionType.PLACE_ORDER, KycLevel.FULL);
        PolicyDecision decision = engine.evaluate(policySet, ctx);
        assertThat(decision.decision()).isEqualTo(PolicyAction.DENY);
        assertThat(decision.matchedRuleCode()).isEqualTo("COUNTRY_TIER_POLICY");
    }

    @Test
    void allowsLoginFromSingapore() {
        EvaluationContext ctx = context("SG", null, ComplianceActionType.LOGIN, KycLevel.NONE);
        PolicyDecision decision = engine.evaluate(policySet, ctx);
        assertThat(decision.decision()).isEqualTo(PolicyAction.ALLOW);
        assertThat(decision.matchedRuleCode()).isEqualTo("DEFAULT_ALLOW");
    }

    @Test
    void failClosedForCriticalActionWithoutExplicitAllow() {
        EvaluationContext ctx = context(null, null, ComplianceActionType.PLACE_ORDER, KycLevel.FULL);
        PolicyDecision decision = engine.evaluate(policySet, ctx);
        assertThat(decision.decision()).isEqualTo(PolicyAction.DENY);
        assertThat(decision.reasonCode()).isEqualTo("DEFAULT_FAIL_CLOSED");
    }

    @Test
    void allowsPlaceOrderForSingaporeWithFullKyc() {
        countryProfileRepository.put(sgProfile());
        EvaluationContext ctx = context("SG", null, ComplianceActionType.PLACE_ORDER, KycLevel.FULL);
        PolicyDecision decision = engine.evaluate(policySet, ctx);
        assertThat(decision.decision()).isEqualTo(PolicyAction.ALLOW);
        assertThat(decision.matchedRuleCode()).isEqualTo("IMPLICIT_ALLOW");
    }

    private EvaluationContext context(String country, String region, ComplianceActionType action, KycLevel kyc) {
        return new EvaluationContext("req-1", "user-1", "0xabc", "1.2.3.4", country, region, action, kyc, List.of());
    }

    private CountryProfileEntity sgProfile() {
        return CountryProfileEntity.builder().countryCode("SG").tier("T1")
                .allowed(true).tradeAllowed(true).custodyAllowed(true).build();
    }

    private PolicyRuleEntity geoBlockRule() {
        return PolicyRuleEntity.builder().ruleCode("CN_MAINLAND_BLOCK").ruleType(RuleType.GEO_BLOCK)
                .conditionsJson(Map.of("blockedCountries", List.of("CN"), "mainlandChina", true))
                .action(PolicyAction.DENY).reasonTemplate("CN blocked").enabled(true).build();
    }

    private PolicyRuleEntity kycTradingRule() {
        return PolicyRuleEntity.builder().ruleCode("KYC_TRADING_GATE").ruleType(RuleType.KYC_REQUIRED)
                .conditionsJson(Map.of("actions", List.of("PLACE_ORDER", "DEPOSIT", "WITHDRAW"), "minKycLevel", "FULL"))
                .action(PolicyAction.DENY).enabled(true).build();
    }

    private PolicyRuleEntity kycViewRule() {
        return PolicyRuleEntity.builder().ruleCode("KYC_VIEW_GATE").ruleType(RuleType.KYC_REQUIRED)
                .conditionsJson(Map.of("actions", List.of("VIEW_MARKET"), "minKycLevel", "BASIC"))
                .action(PolicyAction.DENY).enabled(true).build();
    }

    private PolicyRuleEntity countryTierRule() {
        return PolicyRuleEntity.builder().ruleCode("COUNTRY_TIER_POLICY").ruleType(RuleType.COUNTRY_TIER)
                .conditionsJson(Map.of("applyTierRestrictions", true))
                .action(PolicyAction.DENY).enabled(true).build();
    }

    private PolicyRuleEntity defaultAllowRule() {
        return PolicyRuleEntity.builder().ruleCode("DEFAULT_ALLOW").ruleType(RuleType.ACTION_ALLOWLIST)
                .conditionsJson(Map.of("actions", List.of("LOGIN", "VIEW_MARKET")))
                .action(PolicyAction.ALLOW).enabled(true).build();
    }
}
