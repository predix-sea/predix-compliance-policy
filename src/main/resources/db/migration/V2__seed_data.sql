-- Seed country profiles: SG > TH > MY > PH > VN > ID
INSERT INTO country_profiles (country_code, tier, is_allowed, trade_allowed, custody_allowed, notes) VALUES
    ('SG', 'T1', TRUE, TRUE, TRUE, 'Singapore - highest tier'),
    ('TH', 'T1', TRUE, TRUE, TRUE, 'Thailand'),
    ('MY', 'T2', TRUE, TRUE, TRUE, 'Malaysia'),
    ('PH', 'T2', TRUE, TRUE, FALSE, 'Philippines - no custody'),
    ('VN', 'T3', TRUE, TRUE, FALSE, 'Vietnam - restricted custody'),
    ('ID', 'T3', TRUE, FALSE, FALSE, 'Indonesia - view only trade'),
    ('CN', 'T3', FALSE, FALSE, FALSE, 'China - blocked by geo rule');

-- Global compliance policy v1 (ACTIVE)
INSERT INTO policy_sets (policy_code, name, version, status, effective_from, priority, created_by)
VALUES ('GLOBAL_COMPLIANCE', 'Global Compliance Baseline', 1, 'ACTIVE', NOW(), 10, 'system');

INSERT INTO policy_rules (policy_set_id, rule_code, rule_type, conditions_json, action, reason_template, enabled)
SELECT id, 'CN_MAINLAND_BLOCK', 'GEO_BLOCK',
       '{"blockedCountries":["CN"],"blockedRegions":["CN"],"mainlandChina":true}'::jsonb,
       'DENY', 'Access from mainland China is not permitted ({{countryCode}})', TRUE
FROM policy_sets WHERE policy_code = 'GLOBAL_COMPLIANCE' AND version = 1;

INSERT INTO policy_rules (policy_set_id, rule_code, rule_type, conditions_json, action, reason_template, enabled)
SELECT id, 'WALLET_DENYLIST', 'ACTION_DENYLIST',
       '{"deniedWallets":[]}'::jsonb,
       'DENY', 'Wallet address is on compliance deny list', TRUE
FROM policy_sets WHERE policy_code = 'GLOBAL_COMPLIANCE' AND version = 1;

INSERT INTO policy_rules (policy_set_id, rule_code, rule_type, conditions_json, action, reason_template, enabled)
SELECT id, 'KYC_TRADING_GATE', 'KYC_REQUIRED',
       '{"actions":["PLACE_ORDER","DEPOSIT","WITHDRAW"],"minKycLevel":"FULL"}'::jsonb,
       'DENY', 'KYC level {{kycLevel}} insufficient; FULL required for {{actionType}}', TRUE
FROM policy_sets WHERE policy_code = 'GLOBAL_COMPLIANCE' AND version = 1;

INSERT INTO policy_rules (policy_set_id, rule_code, rule_type, conditions_json, action, reason_template, enabled)
SELECT id, 'KYC_VIEW_GATE', 'KYC_REQUIRED',
       '{"actions":["VIEW_MARKET"],"minKycLevel":"BASIC"}'::jsonb,
       'DENY', 'KYC level {{kycLevel}} insufficient; BASIC required for market view', TRUE
FROM policy_sets WHERE policy_code = 'GLOBAL_COMPLIANCE' AND version = 1;

INSERT INTO policy_rules (policy_set_id, rule_code, rule_type, conditions_json, action, reason_template, enabled)
SELECT id, 'COUNTRY_TIER_POLICY', 'COUNTRY_TIER',
       '{"applyTierRestrictions":true}'::jsonb,
       'DENY', 'Country {{countryCode}} tier {{tier}} does not allow {{actionType}}', TRUE
FROM policy_sets WHERE policy_code = 'GLOBAL_COMPLIANCE' AND version = 1;

INSERT INTO policy_rules (policy_set_id, rule_code, rule_type, conditions_json, action, reason_template, enabled)
SELECT id, 'DEFAULT_ALLOW', 'ACTION_ALLOWLIST',
       '{"actions":["LOGIN","VIEW_MARKET"],"defaultAllow":true}'::jsonb,
       'ALLOW', 'Default allow for low-risk action', TRUE
FROM policy_sets WHERE policy_code = 'GLOBAL_COMPLIANCE' AND version = 1;
