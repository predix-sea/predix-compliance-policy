package com.predix.compliance.audit;

import com.predix.compliance.domain.ComplianceDecisionEntity;
import com.predix.compliance.domain.PolicyAction;
import com.predix.compliance.policy.EvaluationContext;
import com.predix.compliance.policy.PolicyDecision;
import com.predix.compliance.repository.ComplianceDecisionRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
public class DecisionAuditService {

    private final ComplianceDecisionRepository repository;

    public DecisionAuditService(ComplianceDecisionRepository repository) {
        this.repository = repository;
    }

    @Async
    @Transactional
    public void record(EvaluationContext ctx, PolicyDecision decision, String clientIp) {
        repository.save(ComplianceDecisionEntity.builder()
                .requestId(ctx.requestId())
                .subjectId(ctx.subjectId())
                .ipHash(IpMaskingUtil.hash(clientIp))
                .ipMasked(IpMaskingUtil.mask(clientIp))
                .countryCode(ctx.countryCode())
                .region(ctx.region())
                .actionType(ctx.actionType().name())
                .decision(decision.decision().name())
                .matchedPolicyCode(decision.matchedPolicyCode())
                .matchedRuleCode(decision.matchedRuleCode())
                .reason(decision.reasonMessage())
                .policyVersion(decision.policyVersion())
                .build());
    }

    @Transactional(readOnly = true)
    public Page<ComplianceDecisionEntity> search(String subjectId, String actionType, String decision,
                                                   Instant from, Instant to, Pageable pageable) {
        Specification<ComplianceDecisionEntity> spec = Specification.where(null);
        if (subjectId != null && !subjectId.isBlank()) {
            spec = spec.and((root, q, cb) -> cb.equal(root.get("subjectId"), subjectId));
        }
        if (actionType != null && !actionType.isBlank()) {
            spec = spec.and((root, q, cb) -> cb.equal(root.get("actionType"), actionType));
        }
        if (decision != null && !decision.isBlank()) {
            spec = spec.and((root, q, cb) -> cb.equal(root.get("decision"), decision));
        }
        if (from != null) {
            spec = spec.and((root, q, cb) -> cb.greaterThanOrEqualTo(root.get("createdAt"), from));
        }
        if (to != null) {
            spec = spec.and((root, q, cb) -> cb.lessThanOrEqualTo(root.get("createdAt"), to));
        }
        return repository.findAll(spec, pageable);
    }
}
