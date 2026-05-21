package com.predix.compliance.controller;

import com.predix.compliance.dto.PolicyRuleRequest;
import com.predix.compliance.dto.PolicyRuleResponse;
import com.predix.compliance.service.PolicyManagementService;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/rules")
public class RuleAdminController {

    private final PolicyManagementService policyManagementService;

    public RuleAdminController(PolicyManagementService policyManagementService) {
        this.policyManagementService = policyManagementService;
    }

    @PatchMapping("/{ruleId}")
    public PolicyRuleResponse patch(@PathVariable Long ruleId,
                                    @Valid @RequestBody PolicyRuleRequest request,
                                    Authentication auth) {
        return policyManagementService.patchRule(ruleId, request, auth != null ? auth.getName() : "system");
    }
}
