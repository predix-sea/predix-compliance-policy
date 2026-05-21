# predix-compliance-policy

PrediX 独立合规策略中心（Policy Decision Point, PDP）。负责地域访问控制、KYC 门禁、国家分层策略、规则版本化与可审计决策，为 `predix-bff-gateway` 提供低延迟策略评估 API。

## 快速启动

### Docker Compose（推荐）

```bash
mvn -q -DskipTests package
docker compose up --build
```

服务地址：`http://localhost:8095`

- Health: `GET /actuator/health`
- Prometheus: `GET /actuator/prometheus`
- Swagger UI: `http://localhost:8095/swagger-ui.html`

### 本地开发

```bash
# 启动依赖
docker compose up postgres redis -d

# 运行应用
export DATABASE_URL=jdbc:postgresql://localhost:5433/predix_compliance
export REDIS_PORT=6380
mvn spring-boot:run
```

## API 示例：策略评估

```bash
curl -s -X POST http://localhost:8095/api/v1/policy/evaluate \
  -H 'Content-Type: application/json' \
  -H 'X-Request-Id: req-demo-001' \
  -d '{
    "requestId": "req-demo-001",
    "subject": { "userId": "user-123", "walletAddress": "0xabc..." },
    "context": {
      "ip": "203.0.113.10",
      "countryCode": "SG",
      "kycLevel": "FULL",
      "riskFlags": []
    },
    "actionType": "PLACE_ORDER"
  }' | jq
```

响应结构（统一包装）：

```json
{
  "code": "OK",
  "message": "Success",
  "data": {
    "decision": "ALLOW",
    "reasonCode": "ALLOW",
    "reasonMessage": "No deny rule matched; implicit allow",
    "matchedPolicy": "GLOBAL_COMPLIANCE",
    "matchedRule": "IMPLICIT_ALLOW",
    "policyVersion": 1,
    "obligations": []
  },
  "traceId": "req-demo-001",
  "timestamp": "2026-05-20T00:00:00Z"
}
```

中国大陆阻断示例：

```bash
curl -s -X POST http://localhost:8095/api/v1/policy/evaluate \
  -H 'Content-Type: application/json' \
  -d '{
    "subject": { "userId": "u-cn" },
    "context": { "countryCode": "CN", "kycLevel": "FULL" },
    "actionType": "LOGIN"
  }' | jq '.data'
```

## 规则优先级

引擎按以下顺序评估（先命中 DENY/REVIEW 即终止）：

1. **GEO_BLOCK** — 中国大陆 100% 阻断（`CN` / mainland region）
2. **ACTION_DENYLIST** — 钱包/实体黑名单
3. **KYC_REQUIRED** — 交易/资金动作需 `FULL`，浏览需 `BASIC`
4. **COUNTRY_TIER** — 基于 `country_profiles` 的 trade/custody 限制
5. **ACTION_ALLOWLIST** — 显式允许的低风险动作（LOGIN/VIEW_MARKET）
6. **默认策略** — 无拒绝规则时隐式 ALLOW；关键动作在**无法解析国家**时 FAIL_CLOSED

## 策略版本发布流程

1. `POST /api/v1/policies` 创建新版本（`DRAFT`）
2. `POST /api/v1/policies/{code}/rules` 配置规则
3. `PATCH /api/v1/policies/{code}/activate` body: `{ "version": 2 }`
4. 激活后自动：旧 ACTIVE → DEPRECATED，Redis 缓存失效并重载
5. 所有变更写入 `policy_audit_logs`

管理接口需 Header：`X-Admin-Token: <ADMIN_TOKEN>`

## 与 bff-gateway 对接

在 BFF 中通过 HTTP 客户端调用 PDP（建议连接池 + 超时 100ms）：

```yaml
# predix-bff-gateway application.yml 示例
predix:
  services:
    compliancePolicy:
      baseUrl: http://localhost:8095
```

```java
// WebClient 调用示例
EvaluateRequest req = new EvaluateRequest(
    traceId,
    new EvaluateRequest.SubjectDto(userId, wallet),
    new EvaluateRequest.ContextDto(clientIp, country, region, kycLevel, riskFlags, headers),
    actionType
);
EvaluateResponse decision = webClient.post()
    .uri(baseUrl + "/api/v1/policy/evaluate")
    .header("X-Request-Id", traceId)
    .bodyValue(req)
    .retrieve()
    .bodyToMono(ApiResponse.class);
if ("DENY".equals(decision.data().decision())) {
    throw new BffException(mapReason(decision.data().reasonCode()));
}
```

BFF 应在网关层保留 `ComplianceFilter` 作为快速路径，最终以 PDP 决策为准（可逐步迁移）。

## Prometheus / Grafana

指标端点：`GET /actuator/prometheus`

| 指标 | 说明 |
|------|------|
| `compliance_evaluate_total{action,decision}` | 评估次数 |
| `compliance_evaluate_latency_ms` | 评估延迟 |
| `compliance_block_total{reasonCode}` | 阻断次数 |
| `compliance_policy_reload_total` | 策略缓存重载 |
| `compliance_geo_lookup_fail_total` | Geo 解析失败 |

Grafana：添加 Prometheus 数据源，导入 JVM/Spring Boot 面板并添加上述业务指标面板。

## 测试

```bash
mvn verify
```

- 单元测试：规则优先级、CN 阻断、KYC、国家 tier
- 集成测试：Testcontainers (PostgreSQL + Redis)
- JaCoCo 行覆盖率门槛：≥ 80%

## 文档

- [docs/policy-model.md](docs/policy-model.md)
- [docs/evaluation-flow.md](docs/evaluation-flow.md)
- [docs/china-mainland-block.md](docs/china-mainland-block.md)
- [docs/audit-log.md](docs/audit-log.md)
