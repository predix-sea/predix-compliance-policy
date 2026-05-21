package com.predix.compliance.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.Map;

public record PolicyRuleRequest(
        @NotBlank String ruleCode,
        @NotBlank String ruleType,
        @NotNull Map<String, Object> conditionsJson,
        @NotBlank String action,
        String reasonTemplate,
        Boolean enabled
) {}
