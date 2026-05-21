package com.predix.compliance.dto;

import java.time.Instant;

public record PolicySetResponse(
        Long id,
        String policyCode,
        String name,
        Integer version,
        String status,
        Instant effectiveFrom,
        Instant effectiveTo,
        Integer priority,
        String createdBy,
        Instant createdAt,
        Instant updatedAt
) {}
