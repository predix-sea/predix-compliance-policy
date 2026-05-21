package com.predix.compliance.policy;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.predix.compliance.domain.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class RuleEvaluatorTest {

    private RuleEvaluator evaluator;
    private StubCountryProfileRepository repository;
    private LoadedPolicySet policySet;

    @BeforeEach
    void setUp() {
        repository = new StubCountryProfileRepository();
        evaluator = new RuleEvaluator(repository, new ObjectMapper());
        policySet = new LoadedPolicySet(
                PolicySetEntity.builder().policyCode("P").version(1).build(),
                List.of());
    }

    @Test
    void denylistBlocksWallet() {
        PolicyRuleEntity rule = PolicyRuleEntity.builder()
                .ruleCode("DENY_WALLET").ruleType(RuleType.ACTION_DENYLIST)
                .conditionsJson(Map.of("deniedWallets", List.of("0xbad")))
                .action(PolicyAction.DENY).enabled(true).build();
        EvaluationContext ctx = new EvaluationContext("r", "u", "0xbad", "1.1.1.1", "SG", null,
                ComplianceActionType.LOGIN, KycLevel.FULL, List.of());
        Optional<PolicyDecision> decision = evaluator.evaluate(rule, policySet, ctx);
        assertThat(decision).isPresent();
        assertThat(decision.get().decision()).isEqualTo(PolicyAction.DENY);
    }

    @Test
    void countryTierAllowsViewWhenTradeDisabled() {
        repository.put(CountryProfileEntity.builder().countryCode("ID").tier("T3")
                .allowed(true).tradeAllowed(false).custodyAllowed(false).build());
        PolicyRuleEntity rule = PolicyRuleEntity.builder()
                .ruleCode("TIER").ruleType(RuleType.COUNTRY_TIER)
                .conditionsJson(Map.of()).action(PolicyAction.DENY).enabled(true).build();
        EvaluationContext ctx = new EvaluationContext("r", "u", null, "1.1.1.1", "ID", null,
                ComplianceActionType.VIEW_MARKET, KycLevel.FULL, List.of());
        assertThat(evaluator.evaluate(rule, policySet, ctx)).isEmpty();
    }
}
