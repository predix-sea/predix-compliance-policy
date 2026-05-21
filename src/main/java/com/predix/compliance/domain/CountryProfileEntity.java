package com.predix.compliance.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "country_profiles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CountryProfileEntity {

    @Id
    @Column(name = "country_code")
    private String countryCode;

    @Column(nullable = false)
    private String tier;

    @Column(name = "is_allowed", nullable = false)
    private boolean allowed;

    @Column(name = "trade_allowed", nullable = false)
    private boolean tradeAllowed;

    @Column(name = "custody_allowed", nullable = false)
    private boolean custodyAllowed;

    @Lob
    private String notes;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @PrePersist
    @PreUpdate
    void touch() {
        updatedAt = Instant.now();
    }
}
