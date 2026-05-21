package com.predix.compliance.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;

public record PolicySetRequest(
        @NotBlank String policyCode,
        @NotBlank String name,
        @NotNull Integer version,
        Instant effectiveFrom,
        Instant effectiveTo,
        @NotNull Integer priority,
        String createdBy
) {}
