# Audit Log

## 决策审计（compliance_decisions）

- **追加写**，无 UPDATE/DELETE API
- 字段：request_id, subject_id, action_type, decision, matched_policy/rule, policy_version
- IP 脱敏：`203.0.xxx.xxx` + SHA-256 hash 存 `ip_hash`
- 查询：`GET /api/v1/decisions?subjectId=&actionType=&from=&to=&decision=`

## 策略变更审计（policy_audit_logs）

管理操作均记录：

| 字段 | 说明 |
|------|------|
| actor | X-Admin-Token 认证用户 |
| action | CREATE / UPDATE / ACTIVATE / DEPRECATE |
| entity_type | POLICY_SET / POLICY_RULE / COUNTRY_PROFILE |
| before_json / after_json | 变更前后快照 |

## 可追溯性

每次 evaluate 响应包含：

- `matchedPolicy` / `matchedRule` / `policyVersion`
- 请求头 `X-Request-Id` 与 DB `request_id` 一致
- MDC `traceId` 写入应用日志
