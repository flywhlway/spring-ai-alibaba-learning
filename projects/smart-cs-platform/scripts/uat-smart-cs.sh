#!/usr/bin/env bash
# 项目三 smart-cs-platform 真机 curl UAT（端口 19300）
# 用法：bash projects/smart-cs-platform/scripts/uat-smart-cs.sh
# 无 AI_DASHSCOPE_API_KEY 时仅验证 health + login + RBAC；有 Key 时追加 ask/stream/handoff/dashboard
set -uo pipefail

REPO_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/../../.." && pwd)"
cd "$REPO_ROOT"

POM="projects/smart-cs-platform/pom.xml"
PORT=19300
HOST="http://localhost:${PORT}"
LOG_DIR="${REPO_ROOT}/.planning/phases/06-smart-cs-platform/uat-logs"
mkdir -p "$LOG_DIR"

PASS=0
FAIL=0
WARN=0
declare -a FAILURES=()

info()  { printf "\033[1m[%s]\033[0m %s\n" "$(date +%H:%M:%S)" "$*"; }
ok()    { printf "  \033[32m✔\033[0m %s\n" "$1"; PASS=$((PASS + 1)); }
warn()  { printf "  \033[33m⚠\033[0m %s\n" "$1"; WARN=$((WARN + 1)); }
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
  info "前置：检查中间件（PG/Redis/Milvus/ES/Nacos）"
  local missing=0
  lsof -iTCP:5432 -sTCP:LISTEN >/dev/null 2>&1 || { bad "PostgreSQL 5432 未监听"; missing=1; }
  lsof -iTCP:6379 -sTCP:LISTEN >/dev/null 2>&1 || { bad "Redis 6379 未监听"; missing=1; }
  lsof -iTCP:19530 -sTCP:LISTEN >/dev/null 2>&1 || { bad "Milvus 19530 未监听（冷启动 30~60s）"; missing=1; }
  lsof -iTCP:9200 -sTCP:LISTEN >/dev/null 2>&1 || { bad "Elasticsearch 9200 未监听"; missing=1; }
  lsof -iTCP:8848 -sTCP:LISTEN >/dev/null 2>&1 || { bad "Nacos 8848 未监听"; missing=1; }
  if [ "$missing" -ne 0 ]; then
    info "提示：docker compose -f docker/docker-compose.yml \\"
    info "  -f projects/smart-cs-platform/docker-compose.override.yml \\"
    info "  --profile core --profile vector --profile search --profile cloud --profile smartcs up -d"
    exit 1
  fi
  ok "中间件端口就绪"
}

start_app() {
  info "启动 smart-cs-platform（端口 ${PORT}）"
  lsof -tiTCP:"$PORT" -sTCP:LISTEN 2>/dev/null | xargs kill -9 2>/dev/null || true
  local log="${LOG_DIR}/scs-uat.log"
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

extract_token() {
  echo "$1" | grep -o '"accessToken":"[^"]*"' | head -1 | cut -d'"' -f4
}

# ── 主流程 ──
info "========== smart-cs-platform UAT =========="

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

# 1. login admin / agent / customer
raw=$(curl -sS -w "\n%{http_code}" --max-time 30 -X POST "${HOST}/api/auth/login" \
  -H 'Content-Type: application/json' \
  -d '{"username":"admin","password":"admin123"}')
body=$(echo "$raw" | sed '$d')
code=$(echo "$raw" | tail -n1)
ADMIN_TOKEN=$(extract_token "$body")
if [ "$code" = "200" ] && parse_json_code "$body" && [ -n "$ADMIN_TOKEN" ]; then
  ok "POST /api/auth/login (admin) — token 已获取"
else
  bad "POST /api/auth/login (admin) — HTTP ${code}"
fi

raw=$(curl -sS -w "\n%{http_code}" --max-time 30 -X POST "${HOST}/api/auth/login" \
  -H 'Content-Type: application/json' \
  -d '{"username":"agent1","password":"agent123"}')
body=$(echo "$raw" | sed '$d')
code=$(echo "$raw" | tail -n1)
AGENT_TOKEN=$(extract_token "$body")
if [ "$code" = "200" ] && parse_json_code "$body" && [ -n "$AGENT_TOKEN" ]; then
  ok "POST /api/auth/login (agent1) — token 已获取"
else
  bad "POST /api/auth/login (agent1) — HTTP ${code}"
fi

raw=$(curl -sS -w "\n%{http_code}" --max-time 30 -X POST "${HOST}/api/auth/login" \
  -H 'Content-Type: application/json' \
  -d '{"username":"customer1","password":"customer123"}')
body=$(echo "$raw" | sed '$d')
code=$(echo "$raw" | tail -n1)
CUSTOMER_TOKEN=$(extract_token "$body")
if [ "$code" = "200" ] && parse_json_code "$body" && [ -n "$CUSTOMER_TOKEN" ]; then
  ok "POST /api/auth/login (customer1) — token 已获取"
else
  bad "POST /api/auth/login (customer1) — HTTP ${code}"
fi

# customer 访问 admin → 403
raw=$(curl -sS -w "\n%{http_code}" --max-time 30 \
  -H "Authorization: Bearer ${CUSTOMER_TOKEN}" \
  "${HOST}/api/admin/dashboard/stats?days=7")
code=$(echo "$raw" | tail -n1)
if [ "$code" = "403" ]; then
  ok "GET /api/admin/dashboard/stats (customer) — 403 Forbidden"
else
  bad "GET /api/admin/dashboard/stats (customer) — 期望 403，实际 HTTP ${code}"
fi

if [ -z "${AI_DASHSCOPE_API_KEY:-}" ]; then
  info "未设置 AI_DASHSCOPE_API_KEY，跳过 ask/stream/handoff 模型相关用例"
else
  CONV_ID="uat-scs-$(date +%s)"

  # FAQ ask
  raw=$(curl -sS -w "\n%{http_code}" --max-time 120 -X POST "${HOST}/api/chat/ask" \
    -H "Authorization: Bearer ${CUSTOMER_TOKEN}" \
    -H 'Content-Type: application/json' \
    -d "{\"conversationId\":\"${CONV_ID}\",\"question\":\"收到商品后多久可以申请退货？\"}")
  body=$(echo "$raw" | sed '$d')
  code=$(echo "$raw" | tail -n1)
  if [ "$code" = "200" ] && parse_json_code "$body" && echo "$body" | grep -q '"answer"'; then
    ok "POST /api/chat/ask (FAQ) — 返回 answer"
  else
    bad "POST /api/chat/ask (FAQ) — HTTP ${code}"
  fi

  # SSE stream：POST + JSON body，避免问题原文进入 query string / 访问日志
  sse=$(curl -sS --max-time 120 -X POST "${HOST}/api/chat/stream" \
    -H "Authorization: Bearer ${CUSTOMER_TOKEN}" \
    -H 'Content-Type: application/json' \
    -H 'Accept: text/event-stream' \
    -d "{\"conversationId\":\"${CONV_ID}\",\"question\":\"如何查询我的订单物流信息\"}")
  if echo "$sse" | grep -qE 'event:\s*message|event:message' \
      && echo "$sse" | grep -qE 'event:\s*done|event:done'; then
    ok "POST /api/chat/stream — message + done 事件"
  else
    bad "POST /api/chat/stream — SSE 事件不完整"
  fi

  # handoff start + agent approve
  raw=$(curl -sS -w "\n%{http_code}" --max-time 120 -X POST "${HOST}/api/handoff/start" \
    -H "Authorization: Bearer ${CUSTOMER_TOKEN}" \
    -H 'Content-Type: application/json' \
    -d "{\"conversationId\":\"${CONV_ID}\",\"query\":\"我要投诉，请转人工处理\"}")
  body=$(echo "$raw" | sed '$d')
  code=$(echo "$raw" | tail -n1)
  if [ "$code" = "200" ] && parse_json_code "$body"; then
    ok "POST /api/handoff/start — 触发人工接管"
  else
    bad "POST /api/handoff/start — HTTP ${code}"
  fi

  raw=$(curl -sS -w "\n%{http_code}" --max-time 120 -X POST "${HOST}/api/handoff/approve" \
    -H "Authorization: Bearer ${AGENT_TOKEN}" \
    -H 'Content-Type: application/json' \
    -d "{\"threadId\":\"${CONV_ID}\"}")
  body=$(echo "$raw" | sed '$d')
  code=$(echo "$raw" | tail -n1)
  if [ "$code" = "200" ] && parse_json_code "$body"; then
    ok "POST /api/handoff/approve — 坐席确认"
  else
    bad "POST /api/handoff/approve — HTTP ${code} body=${body:0:200}"
  fi

  # admin dashboard
  raw=$(curl -sS -w "\n%{http_code}" --max-time 30 \
    -H "Authorization: Bearer ${ADMIN_TOKEN}" \
    "${HOST}/api/admin/dashboard/stats?days=7")
  body=$(echo "$raw" | sed '$d')
  code=$(echo "$raw" | tail -n1)
  if [ "$code" = "200" ] && parse_json_code "$body"; then
    ok "GET /api/admin/dashboard/stats — 运营看板"
  else
    bad "GET /api/admin/dashboard/stats — HTTP ${code}"
  fi
fi

info "========== 结果：${PASS} 通过 / ${FAIL} 失败 / ${WARN} 警告 =========="
if [ "$FAIL" -gt 0 ]; then
  for f in "${FAILURES[@]}"; do echo "  - $f"; done
  exit 1
fi
