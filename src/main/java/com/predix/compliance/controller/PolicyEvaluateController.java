package com.predix.compliance.controller;

import com.predix.compliance.dto.BatchEvaluateRequest;
import com.predix.compliance.dto.BatchEvaluateResponse;
import com.predix.compliance.dto.EvaluateRequest;
import com.predix.compliance.dto.EvaluateResponse;
import com.predix.compliance.service.PolicyEvaluationService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/v1/policy")
public class PolicyEvaluateController {

    private final PolicyEvaluationService evaluationService;

    public PolicyEvaluateController(PolicyEvaluationService evaluationService) {
        this.evaluationService = evaluationService;
    }

    @PostMapping("/evaluate")
    public EvaluateResponse evaluate(@Valid @RequestBody EvaluateRequest request) {
        return evaluationService.evaluate(request);
    }

    @PostMapping("/evaluate/batch")
    public BatchEvaluateResponse evaluateBatch(@Valid @RequestBody BatchEvaluateRequest request) {
        List<BatchEvaluateResponse.EvaluateResultItem> results = new ArrayList<>();
        for (EvaluateRequest item : request.requests()) {
            EvaluateResponse evaluation = evaluationService.evaluate(item);
            String requestId = item.requestId() != null ? item.requestId() : evaluation.matchedPolicy();
            results.add(new BatchEvaluateResponse.EvaluateResultItem(
                    item.requestId() != null ? item.requestId() : requestId, evaluation));
        }
        return new BatchEvaluateResponse(results);
    }
}
