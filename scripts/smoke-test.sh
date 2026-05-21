#!/usr/bin/env bash
set -euo pipefail
BASE="${1:-http://localhost:8095}"

echo "Health check..."
curl -sf "$BASE/actuator/health" | jq -r .status

echo "Evaluate SG login..."
curl -sf -X POST "$BASE/api/v1/policy/evaluate" \
  -H 'Content-Type: application/json' \
  -d '{"subject":{"userId":"smoke"},"context":{"countryCode":"SG","kycLevel":"NONE"},"actionType":"LOGIN"}' \
  | jq '.data.decision'

echo "Evaluate CN block..."
curl -sf -X POST "$BASE/api/v1/policy/evaluate" \
  -H 'Content-Type: application/json' \
  -d '{"subject":{"userId":"smoke"},"context":{"countryCode":"CN","kycLevel":"FULL"},"actionType":"LOGIN"}' \
  | jq '.data.decision'

echo "OK"
