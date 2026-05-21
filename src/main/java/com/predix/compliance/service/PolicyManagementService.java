package com.predix.compliance.service;

import com.predix.compliance.audit.PolicyAuditService;
import com.predix.compliance.cache.PolicyCacheService;
import com.predix.compliance.domain.*;
import com.predix.compliance.dto.*;
import com.predix.compliance.exception.ComplianceException;
import com.predix.compliance.exception.ErrorCode;
import com.predix.compliance.repository.PolicyRuleRepository;
import com.predix.compliance.repository.PolicySetRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class PolicyManagementService {

    private final PolicySetRepository policySetRepository;
    private final PolicyRuleRepository policyRuleRepository;
    private final PolicyCacheService policyCacheService;
    private final PolicyAuditService policyAuditService;

    public PolicyManagementService(PolicySetRepository policySetRepository,
                                   PolicyRuleRepository policyRuleRepository,
                                   PolicyCacheService policyCacheService,
                                   PolicyAuditService policyAuditService) {
        this.policySetRepository = policySetRepository;
        this.policyRuleRepository = policyRuleRepository;
        this.policyCacheService = policyCacheService;
        this.policyAuditService = policyAuditService;
    }

    @Transactional
    public PolicySetResponse createPolicy(PolicySetRequest request, String actor) {
        policySetRepository.findByPolicyCodeAndVersion(request.policyCode(), request.version())
                .ifPresent(p -> {
                    throw new ComplianceException(ErrorCode.POLICY_VERSION_CONFLICT);
                });

        PolicySetEntity entity = PolicySetEntity.builder()
                .policyCode(request.policyCode())
                .name(request.name())
                .version(request.version())
                .status(PolicyStatus.DRAFT)
                .effectiveFrom(request.effectiveFrom())
                .effectiveTo(request.effectiveTo())
                .priority(request.priority())
                .createdBy(request.createdBy() != null ? request.createdBy() : actor)
                .build();
        PolicySetEntity saved = policySetRepository.save(entity);
        policyAuditService.log(actor, "CREATE", "POLICY_SET", saved.getPolicyCode() + ":" + saved.getVersion(),
                null, toMap(saved));
        return toResponse(saved);
    }

    @Transactional
    public PolicySetResponse activate(String policyCode, int version, String actor) {
        PolicySetEntity target = policySetRepository.findByPolicyCodeAndVersion(policyCode, version)
                .orElseThrow(() -> new ComplianceException(ErrorCode.POLICY_NOT_FOUND));

        policySetRepository.findByPolicyCodeOrderByVersionDesc(policyCode).forEach(p -> {
            if (p.getStatus() == PolicyStatus.ACTIVE && !p.getId().equals(target.getId())) {
                Map<String, Object> before = toMap(p);
                p.setStatus(PolicyStatus.DEPRECATED);
                p.setEffectiveTo(Instant.now());
                policySetRepository.save(p);
                policyAuditService.log(actor, "DEPRECATE", "POLICY_SET", p.getPolicyCode() + ":" + p.getVersion(),
                        before, toMap(p));
            }
        });

        Map<String, Object> before = toMap(target);
        target.setStatus(PolicyStatus.ACTIVE);
        target.setEffectiveFrom(Instant.now());
        PolicySetEntity saved = policySetRepository.save(target);
        policyCacheService.reload(policyCode);
        policyAuditService.log(actor, "ACTIVATE", "POLICY_SET", policyCode + ":" + version, before, toMap(saved));
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<PolicySetResponse> listPolicies() {
        return policySetRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<PolicySetResponse> listVersions(String policyCode) {
        return policySetRepository.findByPolicyCodeOrderByVersionDesc(policyCode).stream()
                .map(this::toResponse).toList();
    }

    @Transactional
    public PolicyRuleResponse addRule(String policyCode, PolicyRuleRequest request, String actor) {
        PolicySetEntity set = policySetRepository.findFirstByPolicyCodeAndStatus(policyCode, PolicyStatus.DRAFT)
                .or(() -> policySetRepository.findByPolicyCodeOrderByVersionDesc(policyCode).stream().findFirst())
                .orElseThrow(() -> new ComplianceException(ErrorCode.POLICY_NOT_FOUND));

        validateRule(request);
        PolicyRuleEntity rule = PolicyRuleEntity.builder()
                .policySetId(set.getId())
                .ruleCode(request.ruleCode())
                .ruleType(RuleType.valueOf(request.ruleType()))
                .conditionsJson(request.conditionsJson())
                .action(PolicyAction.valueOf(request.action()))
                .reasonTemplate(request.reasonTemplate())
                .enabled(request.enabled() == null || request.enabled())
                .build();
        PolicyRuleEntity saved = policyRuleRepository.save(rule);
        policyAuditService.log(actor, "CREATE", "POLICY_RULE", String.valueOf(saved.getId()),
                null, ruleMap(saved));
        return toRuleResponse(saved);
    }

    @Transactional
    public PolicyRuleResponse patchRule(Long ruleId, PolicyRuleRequest request, String actor) {
        PolicyRuleEntity rule = policyRuleRepository.findById(ruleId)
                .orElseThrow(() -> new ComplianceException(ErrorCode.POLICY_NOT_FOUND));
        Map<String, Object> before = ruleMap(rule);
        if (request.conditionsJson() != null) {
            validateRule(request);
            rule.setConditionsJson(request.conditionsJson());
        }
        if (request.action() != null) {
            rule.setAction(PolicyAction.valueOf(request.action()));
        }
        if (request.reasonTemplate() != null) {
            rule.setReasonTemplate(request.reasonTemplate());
        }
        if (request.enabled() != null) {
            rule.setEnabled(request.enabled());
        }
        PolicyRuleEntity saved = policyRuleRepository.save(rule);
        PolicySetEntity set = policySetRepository.findById(saved.getPolicySetId()).orElseThrow();
        if (set.getStatus() == PolicyStatus.ACTIVE) {
            policyCacheService.reload(set.getPolicyCode());
        }
        policyAuditService.log(actor, "UPDATE", "POLICY_RULE", String.valueOf(ruleId), before, ruleMap(saved));
        return toRuleResponse(saved);
    }

    private void validateRule(PolicyRuleRequest request) {
        if (request.conditionsJson() == null) {
            throw new ComplianceException(ErrorCode.RULE_INVALID_CONDITION, "conditions_json required");
        }
    }

    private PolicySetResponse toResponse(PolicySetEntity e) {
        return new PolicySetResponse(e.getId(), e.getPolicyCode(), e.getName(), e.getVersion(),
                e.getStatus().name(), e.getEffectiveFrom(), e.getEffectiveTo(), e.getPriority(),
                e.getCreatedBy(), e.getCreatedAt(), e.getUpdatedAt());
    }

    private PolicyRuleResponse toRuleResponse(PolicyRuleEntity r) {
        return new PolicyRuleResponse(r.getId(), r.getPolicySetId(), r.getRuleCode(), r.getRuleType().name(),
                r.getConditionsJson(), r.getAction().name(), r.getReasonTemplate(), r.isEnabled(),
                r.getCreatedAt(), r.getUpdatedAt());
    }

    private Map<String, Object> toMap(PolicySetEntity e) {
        Map<String, Object> m = new HashMap<>();
        m.put("policyCode", e.getPolicyCode());
        m.put("version", e.getVersion());
        m.put("status", e.getStatus().name());
        return m;
    }

    private Map<String, Object> ruleMap(PolicyRuleEntity r) {
        Map<String, Object> m = new HashMap<>();
        m.put("ruleCode", r.getRuleCode());
        m.put("ruleType", r.getRuleType().name());
        m.put("enabled", r.isEnabled());
        return m;
    }
}
