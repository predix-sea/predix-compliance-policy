# Policy Model

## policy_sets

| 字段 | 说明 |
|------|------|
| policy_code + version | 唯一键，支持多版本 |
| status | DRAFT / ACTIVE / DEPRECATED |
| priority | 多策略集时优先级（数字越小越高） |
| effective_from / effective_to | 生效窗口 |

## policy_rules

| rule_type | 用途 |
|-----------|------|
| GEO_BLOCK | 地域阻断（CN 大陆） |
| KYC_REQUIRED | 按 action 要求最低 KYC |
| COUNTRY_TIER | 结合 country_profiles 限制 trade/custody |
| ACTION_ALLOWLIST | 显式允许低风险动作 |
| ACTION_DENYLIST | 钱包/实体黑名单 |

`conditions_json` 为 JSONB，按类型解析，示例：

```json
{
  "blockedCountries": ["CN"],
  "mainlandChina": true
}
```

## country_profiles

国家分层（PrediX 东南亚优先级）：

| Tier | 国家 | trade | custody |
|------|------|-------|---------|
| T1 | SG, TH | ✓ | ✓ |
| T2 | MY, PH | ✓ | PH 无 custody |
| T3 | VN, ID | ID 无 trade | 受限 |

## compliance_decisions

追加写审计表，不可 UPDATE。IP 存 masked + SHA-256 hash。
