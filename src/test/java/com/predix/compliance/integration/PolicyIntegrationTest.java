package com.predix.compliance.integration;

import com.redis.testcontainers.RedisContainer;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.junit.jupiter.api.condition.EnabledIf;

import java.util.concurrent.TimeUnit;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers(disabledWithoutDocker = true)
@EnabledIf("com.predix.compliance.integration.DockerConditions#dockerAvailable")
class PolicyIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("compliance_test")
            .withUsername("predix")
            .withPassword("predix");

    @Container
    static RedisContainer redis = new RedisContainer("redis:7-alpine");

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.data.redis.host", redis::getHost);
        registry.add("spring.data.redis.port", () -> redis.getMappedPort(6379).toString());
        registry.add("predix.compliance.admin-token", () -> "test-admin-token");
    }

    @Autowired
    private MockMvc mockMvc;

    @Test
    void evaluateBlocksChina() throws Exception {
        String body = """
                {
                  "requestId": "int-cn-1",
                  "subject": {"userId": "u1", "walletAddress": "0x1"},
                  "context": {"ip": "203.0.113.1", "countryCode": "CN", "kycLevel": "FULL"},
                  "actionType": "LOGIN"
                }
                """;
        mockMvc.perform(post("/api/v1/policy/evaluate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.decision").value("DENY"))
                .andExpect(jsonPath("$.data.reasonCode").value("COMPLIANCE_CN_BLOCKED"));
    }

    @Test
    void evaluateAllowsSingaporeLogin() throws Exception {
        String body = """
                {
                  "subject": {"userId": "u2"},
                  "context": {"ip": "127.0.0.1", "countryCode": "SG", "kycLevel": "NONE"},
                  "actionType": "LOGIN"
                }
                """;
        mockMvc.perform(post("/api/v1/policy/evaluate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.decision").value("ALLOW"));
    }

    @Test
    void policyActivateFlow() throws Exception {
        mockMvc.perform(get("/api/v1/policies")
                        .header("X-Admin-Token", "test-admin-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", not(empty())));

        mockMvc.perform(get("/api/v1/policies/GLOBAL_COMPLIANCE/versions")
                        .header("X-Admin-Token", "test-admin-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].policyCode").value("GLOBAL_COMPLIANCE"));
    }

    @Test
    void decisionAuditPersisted() throws Exception {
        String body = """
                {
                  "requestId": "audit-1",
                  "subject": {"userId": "audit-user"},
                  "context": {"countryCode": "CN", "kycLevel": "FULL"},
                  "actionType": "DEPOSIT"
                }
                """;
        mockMvc.perform(post("/api/v1/policy/evaluate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk());

        Awaitility.await().atMost(5, TimeUnit.SECONDS).untilAsserted(() ->
                mockMvc.perform(get("/api/v1/decisions")
                                .param("subjectId", "audit-user")
                                .header("X-Admin-Token", "test-admin-token"))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.data.content[0].decision").value("DENY"))
                        .andExpect(jsonPath("$.data.content[0].subjectId").value("audit-user")));
    }
}
