# China Mainland Block

## 规则

种子规则 `CN_MAINLAND_BLOCK`（`GEO_BLOCK`）在以下任一条件成立时 **DENY**：

- `countryCode == CN`（不区分大小写）
- `region` 以 `CN` 开头（大陆区域标识）
- `conditions_json.mainlandChina == true` 且上述判定成立

适用于所有 `actionType`：LOGIN、VIEW_MARKET、PLACE_ORDER、DEPOSIT、WITHDRAW。

## IP 解析

1. `ClientIpResolver` 在可信代理后才信任 `X-Forwarded-For` 首段
2. `GeoProvider` 将 IP 解析为国家（默认 `static` 映射，可替换 MaxMind 实现）
3. 请求可显式传 `context.countryCode`（BFF 已解析时优先）

## 错误码

- `reasonCode`: `COMPLIANCE_CN_BLOCKED`
- HTTP API 仍返回 200 + `decision: DENY`（PDP 不做 HTTP 403，由 BFF 映射）

## 配置

```yaml
predix:
  compliance:
    trusted-proxies: 127.0.0.1,10.0.0.0/8
    geo:
      provider: static
      static-country-map:
        "203.0.113.1": CN
```

生产请使用真实 GeoIP 数据库并实现 `GeoProvider` bean。
