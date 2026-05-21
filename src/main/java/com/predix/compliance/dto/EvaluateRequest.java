package com.predix.compliance.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.Map;

public record EvaluateRequest(
        String requestId,
        @NotNull @Valid SubjectDto subject,
        @NotNull @Valid ContextDto context,
        @NotBlank String actionType
) {
    public record SubjectDto(
            String userId,
            String walletAddress
    ) {}

    public record ContextDto(
            String ip,
            String countryCode,
            String region,
            String kycLevel,
            List<String> riskFlags,
            Map<String, String> headers
    ) {}
}
