package com.predix.compliance.dto;

import java.time.Instant;

public record CountryProfileResponse(
        String countryCode,
        String tier,
        boolean allowed,
        boolean tradeAllowed,
        boolean custodyAllowed,
        String notes,
        Instant updatedAt
) {}
