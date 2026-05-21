package com.predix.compliance.controller;

import com.predix.compliance.domain.ComplianceDecisionEntity;
import com.predix.compliance.dto.DecisionAuditResponse;
import com.predix.compliance.service.DecisionQueryService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;

@RestController
@RequestMapping("/api/v1/decisions")
public class DecisionAuditController {

    private final DecisionQueryService decisionQueryService;

    public DecisionAuditController(DecisionQueryService decisionQueryService) {
        this.decisionQueryService = decisionQueryService;
    }

    @GetMapping
    public Page<DecisionAuditResponse> search(
            @RequestParam(required = false) String subjectId,
            @RequestParam(required = false) String actionType,
            @RequestParam(required = false) String decision,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return decisionQueryService.search(subjectId, actionType, decision, from, to, PageRequest.of(page, size));
    }
}
