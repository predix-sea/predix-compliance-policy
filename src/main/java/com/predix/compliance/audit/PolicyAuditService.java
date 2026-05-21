package com.predix.compliance.audit;

import com.predix.compliance.domain.PolicyAuditLogEntity;
import com.predix.compliance.repository.PolicyAuditLogRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Service
public class PolicyAuditService {

    private final PolicyAuditLogRepository repository;

    public PolicyAuditService(PolicyAuditLogRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public void log(String actor, String action, String entityType, String entityId,
                    Map<String, Object> before, Map<String, Object> after) {
        repository.save(PolicyAuditLogEntity.builder()
                .actor(actor)
                .action(action)
                .entityType(entityType)
                .entityId(entityId)
                .beforeJson(before)
                .afterJson(after)
                .build());
    }
}
