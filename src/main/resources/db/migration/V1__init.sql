-- Policy sets
CREATE TABLE policy_sets (
    id              BIGSERIAL PRIMARY KEY,
    policy_code     VARCHAR(64)  NOT NULL,
    name            VARCHAR(256) NOT NULL,
    version         INT          NOT NULL,
    status          VARCHAR(32)  NOT NULL,
    effective_from  TIMESTAMPTZ,
    effective_to    TIMESTAMPTZ,
    priority        INT          NOT NULL DEFAULT 100,
    created_by      VARCHAR(128),
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_policy_code_version UNIQUE (policy_code, version),
    CONSTRAINT chk_policy_status CHECK (status IN ('DRAFT', 'ACTIVE', 'DEPRECATED'))
);

CREATE INDEX idx_policy_sets_active ON policy_sets (status, policy_code) WHERE status = 'ACTIVE';

-- Policy rules
CREATE TABLE policy_rules (
    id               BIGSERIAL PRIMARY KEY,
    policy_set_id    BIGINT       NOT NULL REFERENCES policy_sets(id) ON DELETE CASCADE,
    rule_code        VARCHAR(64)  NOT NULL,
    rule_type        VARCHAR(32)  NOT NULL,
    conditions_json  JSONB        NOT NULL DEFAULT '{}',
    action           VARCHAR(16)  NOT NULL,
    reason_template  VARCHAR(512),
    enabled          BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at       TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at       TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_policy_rule_code UNIQUE (policy_set_id, rule_code),
    CONSTRAINT chk_rule_type CHECK (rule_type IN (
        'GEO_BLOCK', 'KYC_REQUIRED', 'COUNTRY_TIER', 'ACTION_ALLOWLIST', 'ACTION_DENYLIST'
    )),
    CONSTRAINT chk_rule_action CHECK (action IN ('ALLOW', 'DENY', 'REVIEW'))
);

CREATE INDEX idx_policy_rules_set ON policy_rules (policy_set_id, enabled);

-- Country profiles
CREATE TABLE country_profiles (
    country_code    VARCHAR(8)   PRIMARY KEY,
    tier            VARCHAR(8)   NOT NULL,
    is_allowed      BOOLEAN      NOT NULL DEFAULT TRUE,
    trade_allowed   BOOLEAN      NOT NULL DEFAULT TRUE,
    custody_allowed BOOLEAN      NOT NULL DEFAULT TRUE,
    notes           TEXT,
    updated_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    CONSTRAINT chk_country_tier CHECK (tier IN ('T1', 'T2', 'T3'))
);

-- Compliance decisions (append-only audit)
CREATE TABLE compliance_decisions (
    id                  BIGSERIAL PRIMARY KEY,
    request_id          VARCHAR(64)  NOT NULL,
    subject_id          VARCHAR(256),
    ip_hash             VARCHAR(128),
    ip_masked           VARCHAR(64),
    country_code        VARCHAR(8),
    region              VARCHAR(64),
    action_type         VARCHAR(32)  NOT NULL,
    decision            VARCHAR(16)  NOT NULL,
    matched_policy_code VARCHAR(64),
    matched_rule_code   VARCHAR(64),
    reason              VARCHAR(512),
    policy_version      INT,
    created_at          TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    CONSTRAINT chk_decision CHECK (decision IN ('ALLOW', 'DENY', 'REVIEW'))
);

CREATE INDEX idx_decisions_subject ON compliance_decisions (subject_id, created_at DESC);
CREATE INDEX idx_decisions_request ON compliance_decisions (request_id);
CREATE INDEX idx_decisions_created ON compliance_decisions (created_at DESC);

-- Policy change audit (admin mutations)
CREATE TABLE policy_audit_logs (
    id           BIGSERIAL PRIMARY KEY,
    actor        VARCHAR(128) NOT NULL,
    action       VARCHAR(64)  NOT NULL,
    entity_type  VARCHAR(64)  NOT NULL,
    entity_id    VARCHAR(128) NOT NULL,
    before_json  JSONB,
    after_json   JSONB,
    created_at   TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_policy_audit_created ON policy_audit_logs (created_at DESC);
