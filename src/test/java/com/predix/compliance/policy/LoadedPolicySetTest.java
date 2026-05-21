package com.predix.compliance.policy;

import com.predix.compliance.domain.PolicySetEntity;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class LoadedPolicySetTest {

    @Test
    void exposesPolicyMetadata() {
        PolicySetEntity set = PolicySetEntity.builder().policyCode("GLOBAL").version(3).build();
        LoadedPolicySet loaded = new LoadedPolicySet(set, List.of());
        assertThat(loaded.policyCode()).isEqualTo("GLOBAL");
        assertThat(loaded.version()).isEqualTo(3);
    }
}
