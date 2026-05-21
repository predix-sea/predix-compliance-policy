package com.predix.compliance.service;

import com.predix.compliance.audit.DecisionAuditService;
import com.predix.compliance.domain.ComplianceDecisionEntity;
import com.predix.compliance.dto.DecisionAuditResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class DecisionQueryService {

    private final DecisionAuditService decisionAuditService;

    public DecisionQueryService(DecisionAuditService decisionAuditService) {
        this.decisionAuditService = decisionAuditService;
    }

    public Page<DecisionAuditResponse> search(String subjectId, String actionType, String decision,
                                              Instant from, Instant to, Pageable pageable) {
        return decisionAuditService.search(subjectId, actionType, decision, from, to, pageable)
                .map(this::toResponse);
    }

    private DecisionAuditResponse toResponse(ComplianceDecisionEntity e) {
        return new DecisionAuditResponse(e.getId(), e.getRequestId(), e.getSubjectId(), e.getIpMasked(),
                e.getCountryCode(), e.getRegion(), e.getActionType(), e.getDecision(),
                e.getMatchedPolicyCode(), e.getMatchedRuleCode(), e.getReason(),
                e.getPolicyVersion(), e.getCreatedAt());
    }
}
