package com.predix.compliance.dto;

import java.time.Instant;
import java.util.Map;

public record PolicyRuleResponse(
        Long id,
        Long policySetId,
        String ruleCode,
        String ruleType,
        Map<String, Object> conditionsJson,
        String action,
        String reasonTemplate,
        boolean enabled,
        Instant createdAt,
        Instant updatedAt
) {}
