package com.predix.compliance.dto;

import java.time.Instant;

public record DecisionAuditResponse(
        Long id,
        String requestId,
        String subjectId,
        String ipMasked,
        String countryCode,
        String region,
        String actionType,
        String decision,
        String matchedPolicyCode,
        String matchedRuleCode,
        String reason,
        Integer policyVersion,
        Instant createdAt
) {}
