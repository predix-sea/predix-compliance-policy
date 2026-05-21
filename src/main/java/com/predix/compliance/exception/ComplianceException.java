package com.predix.compliance.exception;

public class ComplianceException extends RuntimeException {

    private final ErrorCode errorCode;

    public ComplianceException(ErrorCode errorCode) {
        super(errorCode.defaultMessage());
        this.errorCode = errorCode;
    }

    public ComplianceException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }
}
