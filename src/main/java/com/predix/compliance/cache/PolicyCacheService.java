package com.predix.compliance.cache;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.predix.compliance.config.ComplianceProperties;
import com.predix.compliance.domain.PolicyRuleEntity;
import com.predix.compliance.domain.PolicySetEntity;
import com.predix.compliance.domain.PolicyStatus;
import com.predix.compliance.metrics.ComplianceMetrics;
import com.predix.compliance.cache.PolicySnapshot;
import com.predix.compliance.policy.LoadedPolicySet;
import com.predix.compliance.repository.PolicyRuleRepository;
import com.predix.compliance.repository.PolicySetRepository;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Service
public class PolicyCacheService {

    private static final String CACHE_PREFIX = "compliance:policy:active:";

    private final StringRedisTemplate redisTemplate;
    private final PolicySetRepository policySetRepository;
    private final PolicyRuleRepository policyRuleRepository;
    private final ObjectMapper objectMapper;
    private final ComplianceProperties properties;
    private final ComplianceMetrics metrics;

    public PolicyCacheService(StringRedisTemplate redisTemplate,
                              PolicySetRepository policySetRepository,
                              PolicyRuleRepository policyRuleRepository,
                              ObjectMapper objectMapper,
                              ComplianceProperties properties,
                              ComplianceMetrics metrics) {
        this.redisTemplate = redisTemplate;
        this.policySetRepository = policySetRepository;
        this.policyRuleRepository = policyRuleRepository;
        this.objectMapper = objectMapper;
        this.properties = properties;
        this.metrics = metrics;
    }

    public LoadedPolicySet getActivePolicy(String policyCode) {
        Optional<LoadedPolicySet> cached = readFromCache(policyCode);
        if (cached.isPresent()) {
            return cached.get();
        }
        LoadedPolicySet loaded = loadFromDb(policyCode);
        writeToCache(loaded);
        return loaded;
    }

    public void evict(String policyCode) {
        redisTemplate.delete(cacheKey(policyCode));
        metrics.incrementPolicyReload();
    }

    public void reload(String policyCode) {
        evict(policyCode);
        LoadedPolicySet loaded = loadFromDb(policyCode);
        writeToCache(loaded);
        metrics.incrementPolicyReload();
    }

    private LoadedPolicySet loadFromDb(String policyCode) {
        PolicySetEntity set = policySetRepository.findFirstByPolicyCodeAndStatus(policyCode, PolicyStatus.ACTIVE)
                .orElseGet(() -> policySetRepository.findByStatusOrderByPriorityAsc(PolicyStatus.ACTIVE).stream()
                        .min(Comparator.comparingInt(PolicySetEntity::getPriority))
                        .orElseThrow(() -> new IllegalStateException("No active policy set configured")));
        List<PolicyRuleEntity> rules = policyRuleRepository.findByPolicySetIdAndEnabledTrueOrderByIdAsc(set.getId());
        return new LoadedPolicySet(set, rules);
    }

    private Optional<LoadedPolicySet> readFromCache(String policyCode) {
        String json = redisTemplate.opsForValue().get(cacheKey(policyCode));
        if (json == null) {
            return Optional.empty();
        }
        try {
            PolicySnapshot snapshot = objectMapper.readValue(json, PolicySnapshot.class);
            return Optional.of(snapshot.toLoaded());
        } catch (JsonProcessingException e) {
            redisTemplate.delete(cacheKey(policyCode));
            return Optional.empty();
        }
    }

    private void writeToCache(LoadedPolicySet loaded) {
        try {
            String json = objectMapper.writeValueAsString(PolicySnapshot.from(loaded));
            redisTemplate.opsForValue().set(cacheKey(loaded.policyCode()), json, properties.getCacheTtl());
        } catch (JsonProcessingException ignored) {
            // cache miss acceptable
        }
    }

    private String cacheKey(String policyCode) {
        return CACHE_PREFIX + policyCode;
    }
}
