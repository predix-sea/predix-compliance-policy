package com.predix.compliance.controller;

import com.predix.compliance.dto.*;
import com.predix.compliance.service.PolicyManagementService;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/policies")
public class PolicyAdminController {

    private final PolicyManagementService policyManagementService;

    public PolicyAdminController(PolicyManagementService policyManagementService) {
        this.policyManagementService = policyManagementService;
    }

    @PostMapping
    public PolicySetResponse create(@Valid @RequestBody PolicySetRequest request, Authentication auth) {
        return policyManagementService.createPolicy(request, actor(auth));
    }

    @PatchMapping("/{policyCode}/activate")
    public PolicySetResponse activate(@PathVariable String policyCode,
                                      @Valid @RequestBody ActivatePolicyRequest request,
                                      Authentication auth) {
        return policyManagementService.activate(policyCode, request.version(), actor(auth));
    }

    @GetMapping
    public List<PolicySetResponse> list() {
        return policyManagementService.listPolicies();
    }

    @GetMapping("/{policyCode}/versions")
    public List<PolicySetResponse> versions(@PathVariable String policyCode) {
        return policyManagementService.listVersions(policyCode);
    }

    @PostMapping("/{policyCode}/rules")
    public PolicyRuleResponse addRule(@PathVariable String policyCode,
                                      @Valid @RequestBody PolicyRuleRequest request,
                                      Authentication auth) {
        return policyManagementService.addRule(policyCode, request, actor(auth));
    }

    private String actor(Authentication auth) {
        return auth != null ? auth.getName() : "system";
    }
}
