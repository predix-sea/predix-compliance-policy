package com.predix.compliance.cache;

import com.predix.compliance.domain.*;
import com.predix.compliance.policy.LoadedPolicySet;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class PolicySnapshotTest {

    @Test
    void roundTripSnapshot() {
        PolicySetEntity set = PolicySetEntity.builder().policyCode("P").version(2).priority(5).build();
        PolicyRuleEntity rule = PolicyRuleEntity.builder().id(1L).policySetId(10L).ruleCode("R1")
                .ruleType(RuleType.GEO_BLOCK).conditionsJson(Map.of("k", "v"))
                .action(PolicyAction.DENY).reasonTemplate("msg").enabled(true).build();
        LoadedPolicySet loaded = new LoadedPolicySet(set, List.of(rule));

        PolicySnapshot snapshot = PolicySnapshot.from(loaded);
        LoadedPolicySet restored = snapshot.toLoaded();

        assertThat(restored.policyCode()).isEqualTo("P");
        assertThat(restored.version()).isEqualTo(2);
        assertThat(restored.rules()).hasSize(1);
        assertThat(restored.rules().get(0).getRuleCode()).isEqualTo("R1");
    }
}
