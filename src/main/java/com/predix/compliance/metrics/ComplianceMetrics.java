package com.predix.compliance.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
public class ComplianceMetrics {

    private final MeterRegistry registry;

    public ComplianceMetrics(MeterRegistry registry) {
        this.registry = registry;
    }

    public void recordEvaluate(String action, String decision, long latencyMs) {
        Counter.builder("compliance_evaluate_total")
                .tag("action", action)
                .tag("decision", decision)
                .register(registry)
                .increment();
        Timer.builder("compliance_evaluate_latency_ms")
                .register(registry)
                .record(latencyMs, TimeUnit.MILLISECONDS);
    }

    public void incrementBlock(String reasonCode) {
        Counter.builder("compliance_block_total")
                .tag("reasonCode", reasonCode)
                .register(registry)
                .increment();
    }

    public void incrementPolicyReload() {
        Counter.builder("compliance_policy_reload_total")
                .register(registry)
                .increment();
    }

    public void incrementGeoLookupFail() {
        Counter.builder("compliance_geo_lookup_fail_total")
                .register(registry)
                .increment();
    }
}
