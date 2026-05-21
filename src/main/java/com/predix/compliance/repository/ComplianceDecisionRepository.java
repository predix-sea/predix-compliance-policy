package com.predix.compliance.repository;

import com.predix.compliance.domain.ComplianceDecisionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface ComplianceDecisionRepository extends JpaRepository<ComplianceDecisionEntity, Long>,
        JpaSpecificationExecutor<ComplianceDecisionEntity> {
}
