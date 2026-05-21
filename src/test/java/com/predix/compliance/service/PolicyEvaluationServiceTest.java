package com.predix.compliance.service;

import com.predix.compliance.audit.DecisionAuditService;
import com.predix.compliance.cache.PolicyCacheService;
import com.predix.compliance.domain.*;
import com.predix.compliance.dto.EvaluateRequest;
import com.predix.compliance.dto.EvaluateResponse;
import com.predix.compliance.geo.GeoResolutionService;
import com.predix.compliance.metrics.ComplianceMetrics;
import com.predix.compliance.policy.LoadedPolicySet;
import com.predix.compliance.policy.PolicyEvaluationEngine;
import com.predix.compliance.policy.RuleEvaluator;
import com.predix.compliance.policy.StubCountryProfileRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.predix.compliance.config.ComplianceProperties;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

class PolicyEvaluationServiceTest {

    private PolicyEvaluationService service;
    private StubCountryProfileRepository countryRepo;

    @BeforeEach
    void setUp() {
        countryRepo = new StubCountryProfileRepository();
        countryRepo.save(CountryProfileEntity.builder().countryCode("SG").tier("T1")
                .allowed(true).tradeAllowed(true).custodyAllowed(true).build());

        ComplianceProperties props = new ComplianceProperties();
        RuleEvaluator ruleEvaluator = new RuleEvaluator(countryRepo, new ObjectMapper());
        PolicyEvaluationEngine engine = new PolicyEvaluationEngine(ruleEvaluator, props);
        ComplianceMetrics metrics = new ComplianceMetrics(new SimpleMeterRegistry());

        PolicyCacheService cache = new PolicyCacheService(null, null, null, new ObjectMapper(), props, metrics) {
            @Override
            public LoadedPolicySet getActivePolicy(String policyCode) {
                return samplePolicy();
            }
        };

        AtomicReference<ComplianceDecisionEntity> saved = new AtomicReference<>();
        DecisionAuditService audit = new DecisionAuditService(null) {
            @Override
            public void record(com.predix.compliance.policy.EvaluationContext ctx,
                               com.predix.compliance.policy.PolicyDecision decision, String clientIp) {
                saved.set(ComplianceDecisionEntity.builder()
                        .requestId(ctx.requestId())
                        .decision(decision.decision().name())
                        .build());
            }
        };

        GeoResolutionService geo = new GeoResolutionService(ip -> java.util.Optional.empty(), metrics);
        service = new PolicyEvaluationService(cache, engine, geo, audit, metrics);
    }

    @Test
    void evaluateReturnsDenyForChina() {
        EvaluateRequest req = new EvaluateRequest(
                "req-1",
                new EvaluateRequest.SubjectDto("u1", null),
                new EvaluateRequest.ContextDto(null, "CN", null, "FULL", List.of(), Map.of()),
                "LOGIN");
        EvaluateResponse response = service.evaluate(req);
        assertThat(response.decision()).isEqualTo("DENY");
        assertThat(response.reasonCode()).isEqualTo("COMPLIANCE_CN_BLOCKED");
    }

    @Test
    void evaluateAllowsSingaporeTrading() {
        EvaluateRequest req = new EvaluateRequest(
                "req-2",
                new EvaluateRequest.SubjectDto("u2", null),
                new EvaluateRequest.ContextDto(null, "SG", null, "FULL", List.of(), Map.of()),
                "PLACE_ORDER");
        EvaluateResponse response = service.evaluate(req);
        assertThat(response.decision()).isEqualTo("ALLOW");
    }

    private LoadedPolicySet samplePolicy() {
        PolicySetEntity set = PolicySetEntity.builder()
                .policyCode("GLOBAL_COMPLIANCE").version(1).build();
        List<PolicyRuleEntity> rules = List.of(
                PolicyRuleEntity.builder().ruleCode("CN_MAINLAND_BLOCK").ruleType(RuleType.GEO_BLOCK)
                        .conditionsJson(Map.of("blockedCountries", List.of("CN"), "mainlandChina", true))
                        .action(PolicyAction.DENY).enabled(true).build(),
                PolicyRuleEntity.builder().ruleCode("KYC_TRADING_GATE").ruleType(RuleType.KYC_REQUIRED)
                        .conditionsJson(Map.of("actions", List.of("PLACE_ORDER"), "minKycLevel", "FULL"))
                        .action(PolicyAction.DENY).enabled(true).build(),
                PolicyRuleEntity.builder().ruleCode("COUNTRY_TIER_POLICY").ruleType(RuleType.COUNTRY_TIER)
                        .conditionsJson(Map.of()).action(PolicyAction.DENY).enabled(true).build()
        );
        return new LoadedPolicySet(set, rules);
    }
}
