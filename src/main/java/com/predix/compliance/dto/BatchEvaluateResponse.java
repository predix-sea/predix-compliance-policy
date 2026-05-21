package com.predix.compliance.dto;

import java.util.List;

public record BatchEvaluateResponse(
        List<EvaluateResultItem> results
) {
    public record EvaluateResultItem(
            String requestId,
            EvaluateResponse evaluation
    ) {}
}
