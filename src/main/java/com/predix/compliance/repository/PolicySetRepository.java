package com.predix.compliance.repository;

import com.predix.compliance.domain.PolicySetEntity;
import com.predix.compliance.domain.PolicyStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PolicySetRepository extends JpaRepository<PolicySetEntity, Long> {

    Optional<PolicySetEntity> findByPolicyCodeAndVersion(String policyCode, Integer version);

    List<PolicySetEntity> findByPolicyCodeOrderByVersionDesc(String policyCode);

    List<PolicySetEntity> findByStatusOrderByPriorityAsc(PolicyStatus status);

    Optional<PolicySetEntity> findFirstByPolicyCodeAndStatus(String policyCode, PolicyStatus status);
}
