package com.predix.compliance.domain;

public enum ComplianceActionType {
    LOGIN,
    VIEW_MARKET,
    PLACE_ORDER,
    DEPOSIT,
    WITHDRAW;

    public boolean isFundAction() {
        return this == DEPOSIT || this == WITHDRAW;
    }

    public boolean isTradingAction() {
        return this == PLACE_ORDER || isFundAction();
    }
}
