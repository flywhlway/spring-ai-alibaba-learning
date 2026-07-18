#!/usr/bin/env bash
# 项目一 knowledge-qa-platform 真机 curl UAT（端口 19100）
# 用法：bash scripts/uat-knowledge-qa.sh
# 无 AI_DASHSCOPE_API_KEY 时仅验证 health + login；有 Key 时追加 ask/stream + RBAC 403
set -uo pipefail

REPO_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$REPO_ROOT"

POM="projects/knowledge-qa-platform/pom.xml"
PORT=19100
HOST="http://localhost:${PORT}"
LOG_DIR="${REPO_ROOT}/.planning/phases/04-knowledge-qa-platform/uat-logs"
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
  info "前置：检查中间件（PG/Redis/Milvus/MinIO）"
  local missing=0
  lsof -iTCP:5432 -sTCP:LISTEN >/dev/null 2>&1 || { bad "PostgreSQL 5432 未监听"; missing=1; }
  lsof -iTCP:6379 -sTCP:LISTEN >/dev/null 2>&1 || { bad "Redis 6379 未监听"; missing=1; }
  lsof -iTCP:19530 -sTCP:LISTEN >/dev/null 2>&1 || { bad "Milvus 19530 未监听（冷启动 30~60s）"; missing=1; }
  lsof -iTCP:9000 -sTCP:LISTEN >/dev/null 2>&1 || { bad "MinIO 9000 未监听"; missing=1; }
  if [ "$missing" -ne 0 ]; then
    info "提示：docker compose -f docker/docker-compose.yml \\"
    info "  -f projects/knowledge-qa-platform/docker-compose.override.yml \\"
    info "  --profile core --profile vector --profile cloud --profile kqa up -d"
    exit 1
  fi
  ok "中间件端口就绪"
}

start_app() {
  info "启动 knowledge-qa-platform（端口 ${PORT}）"
  lsof -tiTCP:"$PORT" -sTCP:LISTEN 2>/dev/null | xargs kill -9 2>/dev/null || true
  local log="${LOG_DIR}/kqa-uat.log"
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

# ── 主流程 ──
info "========== knowledge-qa-platform UAT =========="

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

# 1. login admin
raw=$(curl -sS -w "\n%{http_code}" --max-time 30 -X POST "${HOST}/api/auth/login" \
  -H 'Content-Type: application/json' \
  -d '{"username":"admin","password":"admin123"}')
body=$(echo "$raw" | sed '$d')
code=$(echo "$raw" | tail -n1)
ADMIN_TOKEN=$(echo "$body" | grep -o '"accessToken":"[^"]*"' | head -1 | cut -d'"' -f4)
if [ "$code" = "200" ] && parse_json_code "$body" && [ -n "$ADMIN_TOKEN" ]; then
  ok "POST /api/auth/login (admin) — token 已获取"
else
  bad "POST /api/auth/login (admin) — HTTP ${code}"
fi

# login zhangsan
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

if [ -z "${AI_DASHSCOPE_API_KEY:-}" ]; then
  info "未设置 AI_DASHSCOPE_API_KEY，跳过 ask/stream/RBAC 用例"
else
  # 2. ask with citations
  raw=$(curl -sS -w "\n%{http_code}" --max-time 120 -X POST "${HOST}/api/qa/ask" \
    -H "Authorization: Bearer ${USER_TOKEN}" \
    -H 'Content-Type: application/json' \
    -d '{"conversationId":"uat-conv-001","question":"员工出差住宿费报销标准是多少？"}')
  body=$(echo "$raw" | sed '$d')
  code=$(echo "$raw" | tail -n1)
  if [ "$code" = "200" ] && parse_json_code "$body" && echo "$body" | grep -q '"citations"'; then
    if echo "$body" | grep -q '"citations":\[\]'; then
      bad "POST /api/qa/ask — citations 为空（需 Milvus 索引就绪）"
    else
      ok "POST /api/qa/ask — citations 非空"
    fi
  else
    bad "POST /api/qa/ask — HTTP ${code}"
  fi

  # 3. SSE stream（中文 query 必须 URL 编码，否则 Tomcat 拒收）
  STREAM_Q=$(python3 -c 'import urllib.parse; print(urllib.parse.quote("智能网关如何恢复出厂设置"))')
  sse=$(curl -sS --max-time 120 -H "Authorization: Bearer ${USER_TOKEN}" \
    -H 'Accept: text/event-stream' \
    "${HOST}/api/qa/stream?conversationId=uat-conv-001&question=${STREAM_Q}")
  if echo "$sse" | grep -q 'event:message' && echo "$sse" | grep -qE 'event:done|event: done'; then
    ok "GET /api/qa/stream — message + done 事件"
  else
    bad "GET /api/qa/stream — SSE 事件不完整"
  fi

  # 4. employee 访问 admin 403
  raw=$(curl -sS -w "\n%{http_code}" --max-time 30 \
    -H "Authorization: Bearer ${USER_TOKEN}" \
    "${HOST}/api/admin/users?page=1&size=10")
  body=$(echo "$raw" | sed '$d')
  code=$(echo "$raw" | tail -n1)
  if [ "$code" = "403" ]; then
    ok "GET /api/admin/users (employee) — 403 Forbidden"
  else
    bad "GET /api/admin/users (employee) — 期望 403，实际 HTTP ${code}"
  fi
fi

info "========== 结果：${PASS} 通过 / ${FAIL} 失败 =========="
if [ "$FAIL" -gt 0 ]; then
  for f in "${FAILURES[@]}"; do echo "  - $f"; done
  exit 1
fi
