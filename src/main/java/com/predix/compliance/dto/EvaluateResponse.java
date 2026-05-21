package com.predix.compliance.dto;

import java.util.List;

public record EvaluateResponse(
        String decision,
        String reasonCode,
        String reasonMessage,
        String matchedPolicy,
        String matchedRule,
        Integer policyVersion,
        List<String> obligations
) {}
