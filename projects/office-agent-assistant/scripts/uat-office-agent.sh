#!/usr/bin/env bash
# 项目二 office-agent-assistant UAT 脚本
set -euo pipefail
ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
BASE_URL="${BASE_URL:-http://localhost:19200}"
cd "$ROOT"

echo "==> [1/5] 健康检查"
curl -sf "$BASE_URL/actuator/health" | grep -q '"status":"UP"'

echo "==> [2/5] 员工登录"
TOKEN=$(curl -sf -X POST "$BASE_URL/api/auth/login" \
  -H 'Content-Type: application/json' \
  -d '{"username":"zhangsan","password":"zhangsan123"}' | python3 -c "import sys,json; print(json.load(sys.stdin)['data']['accessToken'])")

echo "==> [3/5] 当前用户"
curl -sf -H "Authorization: Bearer $TOKEN" "$BASE_URL/api/auth/me" | grep -q 'zhangsan'

echo "==> [4/5] Prompt 模板可读（管理员）"
ADMIN_TOKEN=$(curl -sf -X POST "$BASE_URL/api/auth/login" \
  -H 'Content-Type: application/json' \
  -d '{"username":"admin","password":"admin123"}' | python3 -c "import sys,json; print(json.load(sys.stdin)['data']['accessToken'])")
curl -sf -H "Authorization: Bearer $ADMIN_TOKEN" "$BASE_URL/api/admin/prompts" | grep -q 'meeting-summary'

echo "==> [5/5] 编译测试门禁"
mvn -f projects/office-agent-assistant/pom.xml -q test

echo "UAT PASSED: office-agent-assistant @ $BASE_URL"
