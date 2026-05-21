package com.predix.compliance.exception;

public enum ErrorCode {
    POLICY_NOT_FOUND("POLICY_NOT_FOUND", "Policy not found"),
    POLICY_VERSION_CONFLICT("POLICY_VERSION_CONFLICT", "Policy version already exists"),
    RULE_INVALID_CONDITION("RULE_INVALID_CONDITION", "Rule condition JSON is invalid"),
    GEO_RESOLUTION_FAILED("GEO_RESOLUTION_FAILED", "Unable to resolve geo from IP"),
    COMPLIANCE_CN_BLOCKED("COMPLIANCE_CN_BLOCKED", "Mainland China access blocked"),
    KYC_LEVEL_INSUFFICIENT("KYC_LEVEL_INSUFFICIENT", "KYC level insufficient"),
    UNAUTHORIZED("UNAUTHORIZED", "Unauthorized"),
    VALIDATION_ERROR("VALIDATION_ERROR", "Validation failed"),
    INTERNAL_ERROR("INTERNAL_ERROR", "Internal server error");

    private final String code;
    private final String defaultMessage;

    ErrorCode(String code, String defaultMessage) {
        this.code = code;
        this.defaultMessage = defaultMessage;
    }

    public String code() {
        return code;
    }

    public String defaultMessage() {
        return defaultMessage;
    }
}
