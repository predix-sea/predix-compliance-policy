package com.predix.compliance.dto;

import jakarta.validation.constraints.NotNull;

public record ActivatePolicyRequest(@NotNull Integer version) {}
