package com.predix.compliance.service;

import com.predix.compliance.audit.PolicyAuditService;
import com.predix.compliance.domain.CountryProfileEntity;
import com.predix.compliance.dto.CountryProfilePatchRequest;
import com.predix.compliance.dto.CountryProfileResponse;
import com.predix.compliance.exception.ComplianceException;
import com.predix.compliance.exception.ErrorCode;
import com.predix.compliance.repository.CountryProfileRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class CountryProfileService {

    private final CountryProfileRepository repository;
    private final PolicyAuditService policyAuditService;

    public CountryProfileService(CountryProfileRepository repository, PolicyAuditService policyAuditService) {
        this.repository = repository;
        this.policyAuditService = policyAuditService;
    }

    @Transactional(readOnly = true)
    public List<CountryProfileResponse> listAll() {
        return repository.findAll().stream().map(this::toResponse).toList();
    }

    @Transactional
    public CountryProfileResponse patch(String countryCode, CountryProfilePatchRequest request, String actor) {
        CountryProfileEntity entity = repository.findById(countryCode.toUpperCase())
                .orElseThrow(() -> new ComplianceException(ErrorCode.POLICY_NOT_FOUND, "Country not found"));
        Map<String, Object> before = profileMap(entity);
        if (request.tier() != null) {
            entity.setTier(request.tier());
        }
        if (request.allowed() != null) {
            entity.setAllowed(request.allowed());
        }
        if (request.tradeAllowed() != null) {
            entity.setTradeAllowed(request.tradeAllowed());
        }
        if (request.custodyAllowed() != null) {
            entity.setCustodyAllowed(request.custodyAllowed());
        }
        if (request.notes() != null) {
            entity.setNotes(request.notes());
        }
        CountryProfileEntity saved = repository.save(entity);
        policyAuditService.log(actor, "UPDATE", "COUNTRY_PROFILE", countryCode, before, profileMap(saved));
        return toResponse(saved);
    }

    private CountryProfileResponse toResponse(CountryProfileEntity e) {
        return new CountryProfileResponse(e.getCountryCode(), e.getTier(), e.isAllowed(),
                e.isTradeAllowed(), e.isCustodyAllowed(), e.getNotes(), e.getUpdatedAt());
    }

    private Map<String, Object> profileMap(CountryProfileEntity e) {
        Map<String, Object> m = new HashMap<>();
        m.put("countryCode", e.getCountryCode());
        m.put("tier", e.getTier());
        m.put("allowed", e.isAllowed());
        return m;
    }
}
