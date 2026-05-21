package com.predix.compliance.service;

import com.predix.compliance.audit.DecisionAuditService;
import com.predix.compliance.cache.PolicyCacheService;
import com.predix.compliance.domain.ComplianceActionType;
import com.predix.compliance.domain.KycLevel;
import com.predix.compliance.domain.PolicyAction;
import com.predix.compliance.dto.EvaluateRequest;
import com.predix.compliance.dto.EvaluateResponse;
import com.predix.compliance.geo.GeoResolution;
import com.predix.compliance.geo.GeoResolutionService;
import com.predix.compliance.metrics.ComplianceMetrics;
import com.predix.compliance.policy.EvaluationContext;
import com.predix.compliance.policy.LoadedPolicySet;
import com.predix.compliance.policy.PolicyDecision;
import com.predix.compliance.policy.PolicyEvaluationEngine;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class PolicyEvaluationService {

    private static final String DEFAULT_POLICY_CODE = "GLOBAL_COMPLIANCE";

    private final PolicyCacheService policyCacheService;
    private final PolicyEvaluationEngine evaluationEngine;
    private final GeoResolutionService geoResolutionService;
    private final DecisionAuditService decisionAuditService;
    private final ComplianceMetrics metrics;

    public PolicyEvaluationService(PolicyCacheService policyCacheService,
                                   PolicyEvaluationEngine evaluationEngine,
                                   GeoResolutionService geoResolutionService,
                                   DecisionAuditService decisionAuditService,
                                   ComplianceMetrics metrics) {
        this.policyCacheService = policyCacheService;
        this.evaluationEngine = evaluationEngine;
        this.geoResolutionService = geoResolutionService;
        this.decisionAuditService = decisionAuditService;
        this.metrics = metrics;
    }

    public EvaluateResponse evaluate(EvaluateRequest request) {
        long start = System.currentTimeMillis();
        String requestId = request.requestId() != null && !request.requestId().isBlank()
                ? request.requestId() : UUID.randomUUID().toString();

        String ip = request.context().ip();
        String country = request.context().countryCode();
        String region = request.context().region();

        if (country == null || country.isBlank()) {
            GeoResolution geo = geoResolutionService.resolve(ip).orElse(null);
            if (geo != null) {
                country = geo.countryCode();
                if (region == null) {
                    region = geo.region();
                }
            }
        }

        EvaluationContext ctx = new EvaluationContext(
                requestId,
                request.subject().userId(),
                request.subject().walletAddress(),
                ip,
                country,
                region,
                ComplianceActionType.valueOf(request.actionType().toUpperCase()),
                KycLevel.fromString(request.context().kycLevel()),
                request.context().riskFlags() != null ? request.context().riskFlags() : java.util.List.of()
        );

        LoadedPolicySet policySet = policyCacheService.getActivePolicy(DEFAULT_POLICY_CODE);
        PolicyDecision decision = evaluationEngine.evaluate(policySet, ctx);

        decisionAuditService.record(ctx, decision, ip);

        long latency = System.currentTimeMillis() - start;
        metrics.recordEvaluate(ctx.actionType().name(), decision.decision().name(), latency);
        if (decision.decision() == PolicyAction.DENY) {
            metrics.incrementBlock(decision.reasonCode());
        }

        return new EvaluateResponse(
                decision.decision().name(),
                decision.reasonCode(),
                decision.reasonMessage(),
                decision.matchedPolicyCode(),
                decision.matchedRuleCode(),
                decision.policyVersion(),
                decision.obligations()
        );
    }
}
