package com.predix.compliance.domain;

public enum KycLevel {
    NONE(0),
    BASIC(1),
    FULL(2);

    private final int rank;

    KycLevel(int rank) {
        this.rank = rank;
    }

    public boolean satisfies(KycLevel required) {
        return this.rank >= required.rank;
    }

    public static KycLevel fromString(String value) {
        if (value == null || value.isBlank()) {
            return NONE;
        }
        return KycLevel.valueOf(value.toUpperCase());
    }
}
