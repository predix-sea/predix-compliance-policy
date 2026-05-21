package com.predix.compliance.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record BatchEvaluateRequest(
        @NotEmpty List<@Valid EvaluateRequest> requests
) {}
