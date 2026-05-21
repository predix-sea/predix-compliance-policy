package com.predix.compliance.dto;

public record CountryProfilePatchRequest(
        String tier,
        Boolean allowed,
        Boolean tradeAllowed,
        Boolean custodyAllowed,
        String notes
) {}
