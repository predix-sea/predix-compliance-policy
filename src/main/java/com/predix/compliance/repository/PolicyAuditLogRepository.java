package com.predix.compliance.repository;

import com.predix.compliance.domain.PolicyAuditLogEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PolicyAuditLogRepository extends JpaRepository<PolicyAuditLogEntity, Long> {
}
