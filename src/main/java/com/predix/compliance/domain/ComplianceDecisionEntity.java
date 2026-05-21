package com.predix.compliance.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "compliance_decisions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ComplianceDecisionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "request_id", nullable = false)
    private String requestId;

    @Column(name = "subject_id")
    private String subjectId;

    @Column(name = "ip_hash")
    private String ipHash;

    @Column(name = "ip_masked")
    private String ipMasked;

    @Column(name = "country_code")
    private String countryCode;

    private String region;

    @Column(name = "action_type", nullable = false)
    private String actionType;

    @Column(nullable = false)
    private String decision;

    @Column(name = "matched_policy_code")
    private String matchedPolicyCode;

    @Column(name = "matched_rule_code")
    private String matchedRuleCode;

    private String reason;

    @Column(name = "policy_version")
    private Integer policyVersion;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @PrePersist
    void onCreate() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }
}
