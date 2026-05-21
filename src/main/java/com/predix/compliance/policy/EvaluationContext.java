package com.predix.compliance.policy;

import com.predix.compliance.domain.ComplianceActionType;
import com.predix.compliance.domain.KycLevel;

import java.util.List;

public record EvaluationContext(
        String requestId,
        String userId,
        String walletAddress,
        String clientIp,
        String countryCode,
        String region,
        ComplianceActionType actionType,
        KycLevel kycLevel,
        List<String> riskFlags
) {
    public String subjectId() {
        if (userId != null && !userId.isBlank()) {
            return userId;
        }
        return walletAddress;
    }

    public boolean isMainlandChina() {
        if ("CN".equalsIgnoreCase(countryCode)) {
            return true;
        }
        return region != null && region.toUpperCase().startsWith("CN");
    }
}
