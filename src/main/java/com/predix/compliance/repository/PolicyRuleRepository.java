package com.predix.compliance.repository;

import com.predix.compliance.domain.PolicyRuleEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PolicyRuleRepository extends JpaRepository<PolicyRuleEntity, Long> {

    List<PolicyRuleEntity> findByPolicySetIdAndEnabledTrueOrderByIdAsc(Long policySetId);

    List<PolicyRuleEntity> findByPolicySetIdOrderByIdAsc(Long policySetId);

    Optional<PolicyRuleEntity> findByPolicySetIdAndRuleCode(Long policySetId, String ruleCode);
}
