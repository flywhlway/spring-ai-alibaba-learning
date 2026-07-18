#!/usr/bin/env bash
# 项目二 office-agent-assistant 真机 curl UAT（端口 19200）
# 用法：bash projects/office-agent-assistant/scripts/uat-office-agent.sh
# 无 AI_DASHSCOPE_API_KEY 时仅验证 health + login + prompts + mvn test；
# 有 Key 时追加 chat / tasks / approvals / RBAC。
set -uo pipefail

REPO_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/../../.." && pwd)"
cd "$REPO_ROOT"

POM="projects/office-agent-assistant/pom.xml"
PORT=19200
HOST="${BASE_URL:-http://localhost:${PORT}}"
LOG_DIR="${REPO_ROOT}/.planning/phases/05-office-agent-assistant/uat-logs"
mkdir -p "$LOG_DIR"

PASS=0
FAIL=0
declare -a FAILURES=()

info()  { printf "\033[1m[%s]\033[0m %s\n" "$(date +%H:%M:%S)" "$*"; }
ok()    { printf "  \033[32m✔\033[0m %s\n" "$1"; PASS=$((PASS + 1)); }
bad()   { printf "  \033[31m✘\033[0m %s\n" "$1"; FAIL=$((FAIL + 1)); FAILURES+=("$1"); }

APP_PID=""

cleanup() {
  if [ -n "$APP_PID" ]; then
    kill "$APP_PID" 2>/dev/null || true
    wait "$APP_PID" 2>/dev/null || true
  fi
  lsof -tiTCP:"$PORT" -sTCP:LISTEN 2>/dev/null | xargs kill -9 2>/dev/null || true
}
trap cleanup EXIT

wait_health() {
  local timeout=${1:-180} i=0
  while [ "$i" -lt "$timeout" ]; do
    if curl -sf "${HOST}/actuator/health" >/dev/null 2>&1; then
      return 0
    fi
    sleep 2
    i=$((i + 2))
  done
  return 1
}

check_infra() {
  info "前置：检查中间件（MySQL/Redis/PostgreSQL）"
  local missing=0
  lsof -iTCP:3306 -sTCP:LISTEN >/dev/null 2>&1 || { bad "MySQL 3306 未监听"; missing=1; }
  lsof -iTCP:6379 -sTCP:LISTEN >/dev/null 2>&1 || { bad "Redis 6379 未监听"; missing=1; }
  lsof -iTCP:5432 -sTCP:LISTEN >/dev/null 2>&1 || { bad "PostgreSQL 5432 未监听（pgvector）"; missing=1; }
  if [ "$missing" -ne 0 ]; then
    info "提示：docker compose -f docker/docker-compose.yml \\"
    info "  -f projects/office-agent-assistant/docker-compose.override.yml \\"
    info "  --profile core --profile office up -d"
    exit 1
  fi
  ok "中间件端口就绪"
}

start_app() {
  info "启动 office-agent-assistant（端口 ${PORT}）"
  lsof -tiTCP:"$PORT" -sTCP:LISTEN 2>/dev/null | xargs kill -9 2>/dev/null || true
  local log="${LOG_DIR}/office-uat.log"
  : >"$log"
  mvn -f "$POM" spring-boot:run -DskipTests -q >>"$log" 2>&1 &
  APP_PID=$!
  if ! wait_health 180; then
    bad "应用启动超时，日志：${log}"
    exit 1
  fi
  ok "应用 health 就绪"
}

parse_json_code() {
  echo "$1" | grep -Eq '"code"\s*:\s*0'
}

info "========== office-agent-assistant UAT =========="

check_infra

info "前置：安装 common + starter"
mvn -pl common,starter -am -q -DskipTests install || exit 1

start_app

# 0. health
raw=$(curl -sS -w "\n%{http_code}" --max-time 30 "${HOST}/actuator/health")
body=$(echo "$raw" | sed '$d')
code=$(echo "$raw" | tail -n1)
if [ "$code" = "200" ] && echo "$body" | grep -q '"status":"UP"'; then
  ok "GET /actuator/health — UP"
else
  bad "GET /actuator/health — HTTP ${code}"
fi

# 1. login employee
raw=$(curl -sS -w "\n%{http_code}" --max-time 30 -X POST "${HOST}/api/auth/login" \
  -H 'Content-Type: application/json' \
  -d '{"username":"zhangsan","password":"zhangsan123"}')
body=$(echo "$raw" | sed '$d')
code=$(echo "$raw" | tail -n1)
USER_TOKEN=$(echo "$body" | grep -o '"accessToken":"[^"]*"' | head -1 | cut -d'"' -f4)
if [ "$code" = "200" ] && parse_json_code "$body" && [ -n "$USER_TOKEN" ]; then
  ok "POST /api/auth/login (zhangsan) — token 已获取"
else
  bad "POST /api/auth/login (zhangsan) — HTTP ${code}"
fi

# 2. me
raw=$(curl -sS -w "\n%{http_code}" --max-time 30 \
  -H "Authorization: Bearer ${USER_TOKEN}" "${HOST}/api/auth/me")
body=$(echo "$raw" | sed '$d')
code=$(echo "$raw" | tail -n1)
if [ "$code" = "200" ] && echo "$body" | grep -q 'zhangsan'; then
  ok "GET /api/auth/me — zhangsan"
else
  bad "GET /api/auth/me — HTTP ${code}"
fi

# 3. admin prompts
raw=$(curl -sS -w "\n%{http_code}" --max-time 30 -X POST "${HOST}/api/auth/login" \
  -H 'Content-Type: application/json' \
  -d '{"username":"admin","password":"admin123"}')
body=$(echo "$raw" | sed '$d')
code=$(echo "$raw" | tail -n1)
ADMIN_TOKEN=$(echo "$body" | grep -o '"accessToken":"[^"]*"' | head -1 | cut -d'"' -f4)
if [ "$code" = "200" ] && [ -n "$ADMIN_TOKEN" ]; then
  ok "POST /api/auth/login (admin) — token 已获取"
else
  bad "POST /api/auth/login (admin) — HTTP ${code}"
fi

raw=$(curl -sS -w "\n%{http_code}" --max-time 30 \
  -H "Authorization: Bearer ${ADMIN_TOKEN}" "${HOST}/api/admin/prompts")
body=$(echo "$raw" | sed '$d')
code=$(echo "$raw" | tail -n1)
if [ "$code" = "200" ] && echo "$body" | grep -q 'meeting-summary'; then
  ok "GET /api/admin/prompts — 含 meeting-summary"
else
  bad "GET /api/admin/prompts — HTTP ${code}"
fi

# 4. RBAC：员工访问后台 403
raw=$(curl -sS -w "\n%{http_code}" --max-time 30 \
  -H "Authorization: Bearer ${USER_TOKEN}" \
  "${HOST}/api/admin/users?page=1&size=10")
code=$(echo "$raw" | tail -n1)
if [ "$code" = "403" ]; then
  ok "GET /api/admin/users (employee) — 403 Forbidden"
else
  bad "GET /api/admin/users (employee) — 期望 403，实际 HTTP ${code}"
fi

if [ -z "${AI_DASHSCOPE_API_KEY:-}" ]; then
  info "未设置 AI_DASHSCOPE_API_KEY，跳过 chat/tasks/approvals"
else
  # 5. meeting-summary
  raw=$(curl -sS -w "\n%{http_code}" --max-time 120 -X POST "${HOST}/api/tasks/meeting-summary" \
    -H "Authorization: Bearer ${USER_TOKEN}" \
    -H 'Content-Type: application/json' \
    -d '{"title":"UAT 策略会","input":"讨论华东区目标，张三负责方案，7月15日前提交。"}')
  body=$(echo "$raw" | sed '$d')
  code=$(echo "$raw" | tail -n1)
  if [ "$code" = "200" ] && parse_json_code "$body"; then
    ok "POST /api/tasks/meeting-summary — code=0"
  else
    bad "POST /api/tasks/meeting-summary — HTTP ${code}"
  fi

  # 6. chat（SQL tool 路径）
  raw=$(curl -sS -w "\n%{http_code}" --max-time 180 -X POST "${HOST}/api/chat" \
    -H "Authorization: Bearer ${USER_TOKEN}" \
    -H 'Content-Type: application/json' \
    -d '{"message":"帮我查一下华东销售数据","conversationId":"uat-office-conv-001"}')
  body=$(echo "$raw" | sed '$d')
  code=$(echo "$raw" | tail -n1)
  if [ "$code" = "200" ] && parse_json_code "$body"; then
    ok "POST /api/chat — code=0"
  else
    bad "POST /api/chat — HTTP ${code}"
  fi

  # 7. approval review
  raw=$(curl -sS -w "\n%{http_code}" --max-time 180 -X POST "${HOST}/api/approvals/review" \
    -H "Authorization: Bearer ${USER_TOKEN}" \
    -H 'Content-Type: application/json' \
    -d '{"approvalId":1}')
  body=$(echo "$raw" | sed '$d')
  code=$(echo "$raw" | tail -n1)
  if [ "$code" = "200" ] && parse_json_code "$body"; then
    ok "POST /api/approvals/review — code=0"
  else
    bad "POST /api/approvals/review — HTTP ${code}"
  fi
fi

# 8. 编译/测试门禁（不依赖运行中的应用）
info "编译测试门禁"
if mvn -f "$POM" -q test; then
  ok "mvn test — 通过"
else
  bad "mvn test — 失败"
fi

info "========== 结果：${PASS} 通过 / ${FAIL} 失败 =========="
if [ "$FAIL" -gt 0 ]; then
  for f in "${FAILURES[@]}"; do echo "  - $f"; done
  exit 1
fi
echo "UAT PASSED: office-agent-assistant @ $HOST"
