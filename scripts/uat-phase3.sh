#!/usr/bin/env bash
# Phase 3 全量真机 curl UAT（48 Demo）
# 用法：bash scripts/uat-phase3.sh
set -uo pipefail

REPO_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$REPO_ROOT"

RESULTS_DIR="${REPO_ROOT}/.planning/phases/03-48-demo"
LOG_DIR="${RESULTS_DIR}/uat-logs"
mkdir -p "$LOG_DIR"

PASS=0
FAIL=0
declare -a FAILURES=()

info()  { printf "\033[1m[%s]\033[0m %s\n" "$(date +%H:%M:%S)" "$*"; }
ok()    { printf "  \033[32m✔\033[0m %s\n" "$1"; PASS=$((PASS + 1)); }
bad()   { printf "  \033[31m✘\033[0m %s\n" "$1"; FAIL=$((FAIL + 1)); FAILURES+=("$1"); }

kill_port() {
  lsof -tiTCP:"$1" -sTCP:LISTEN 2>/dev/null | xargs kill -9 2>/dev/null || true
  sleep 1
}

wait_started() {
  local port=$1 log=$2 timeout=${3:-150}
  local i=0
  while [ "$i" -lt "$timeout" ]; do
    if grep -q "Started.*Application" "$log" 2>/dev/null; then
      sleep 2
      return 0
    fi
    if grep -qE "APPLICATION FAILED TO START|BUILD FAILURE" "$log" 2>/dev/null; then
      return 1
    fi
    sleep 2
    i=$((i + 2))
  done
  lsof -iTCP:"$port" -sTCP:LISTEN >/dev/null 2>&1
}

start_app() {
  local pom=$1 port=$2 name=$3 timeout=${4:-150}
  kill_port "$port"
  local log="${LOG_DIR}/${name}.log"
  : >"$log"
  mvn -f "$pom" spring-boot:run -DskipTests -q >>"$log" 2>&1 &
  local pid=$!
  if ! wait_started "$port" "$log" "$timeout"; then
    kill "$pid" 2>/dev/null || true
    kill_port "$port"
    return 1
  fi
  echo "$pid"
}

stop_app() { kill_port "$1"; }

# GET with URL-encoded query params: curl_q URL k1=v1 k2=v2 ...
curl_q() {
  local url=$1 timeout=${2:-90}; shift 2
  local args=(-sS -G "$url" -w "\n%{http_code}" --max-time "$timeout")
  while [ $# -gt 0 ]; do
    local kv=$1; shift
    local k="${kv%%=*}" v="${kv#*=}"
    args+=(--data-urlencode "${k}=${v}")
  done
  curl "${args[@]}"
}

curl_post_json() {
  local url=$1 json=$2 timeout=${3:-90}
  curl -sS -w "\n%{http_code}" --max-time "$timeout" -X POST "$url" \
    -H 'Content-Type: application/json' -d "$json"
}

curl_post_empty() {
  curl -sS -w "\n%{http_code}" --max-time "${2:-90}" -X POST "$1"
}

parse_response() {
  HTTP_CODE=$(echo "$1" | tail -n1)
  BODY=$(echo "$1" | sed '$d')
}

validate_ok() {
  local body=$1 code=$2
  [[ "$code" == "200" ]] || return 1
  [[ ${#body} -gt 3 ]] || return 1
  if echo "$body" | grep -q '"code"'; then
    echo "$body" | grep -Eq '"code"\s*:\s*0' && return 0
    return 1
  fi
  return 0
}

validate_sse() {
  echo "$1" | grep -qE '(event:message|event: done|data:)'
}

run_single() {
  local num=$1 name=$2 pom=$3 port=$4
  shift 4
  local timeout=${1:-150}; shift || true
  info "Demo ${num} ${name} (port ${port})"
  if ! start_app "$pom" "$port" "${num}-${name}" "$timeout" >/dev/null; then
    bad "${num} ${name} — 启动失败"
    return
  fi
  local raw
  raw=$("$@") || true
  parse_response "$raw"
  if validate_ok "$BODY" "$HTTP_CODE"; then
    ok "${num} ${name} — HTTP ${HTTP_CODE}"
  else
    bad "${num} ${name} — HTTP ${HTTP_CODE}, body=${BODY:0:200}"
  fi
  stop_app "$port"
}

# ── 前置 ──
info "前置：环境变量"
[ -n "${AI_DASHSCOPE_API_KEY:-}" ] || { echo "AI_DASHSCOPE_API_KEY 未设置" >&2; exit 1; }

if lsof -iTCP:8848 -sTCP:LISTEN >/dev/null 2>&1; then
  info "前置：Nacos 开发用户"
  bash "${REPO_ROOT}/scripts/nacos-init-dev.sh" || true
fi

info "前置：安装 common + starter"
mvn -pl common,starter -am -q -DskipTests install || exit 1

if ! lsof -iTCP:6380 -sTCP:LISTEN >/dev/null 2>&1; then
  info "前置：启动 Redis Stack（Demo 25，6380）"
  docker compose -f examples/25-redis-vector-demo/docker-compose.override.yml up -d
  sleep 5
fi

info "========== Phase 3 全量 UAT 开始 =========="

run_single 01 quickstart examples/01-quickstart-demo/pom.xml 18001 120 \
  curl_q http://localhost:18001/chat 120 message=hello

run_single 02 autoconfig examples/02-autoconfig-demo/pom.xml 18002 90 \
  curl_q http://localhost:18002/greet 90 name=flywhl

run_single 03 multi-model examples/03-multi-model-demo/pom.xml 18003 120 \
  curl_q http://localhost:18003/chat/dashscope 120 message=hello

run_single 04 chat examples/04-chat-demo/pom.xml 18004 120 \
  curl_q http://localhost:18004/chat/simple 120 message=hello

run_single 05 retry examples/05-retry-demo/pom.xml 18005 120 \
  curl_q http://localhost:18005/chat 120 message=hello

run_single 06 prompt examples/06-prompt-demo/pom.xml 18006 120 \
  curl_q http://localhost:18006/prompt/few-shot 120 dtcCode=P0420

# 07 需先注册模板
info "Demo 07 prompt-builder (port 18007)"
if start_app examples/07-prompt-builder-demo/pom.xml 18007 07-prompt-builder 120 >/dev/null; then
  r1=$(curl_post_json http://localhost:18007/prompts '{"name":"greeting","version":"v1","template":"Hello {assistant_name}"}' 30)
  parse_response "$r1"
  r2=$(curl_post_json http://localhost:18007/prompts/greeting/v1/invoke '{"params":{"assistant_name":"AI"}}' 120)
  parse_response "$r2"
  if validate_ok "$BODY" "$HTTP_CODE"; then ok "07 prompt-builder — invoke OK"; else bad "07 prompt-builder — HTTP ${HTTP_CODE}"; fi
else bad "07 prompt-builder — 启动失败"; fi
stop_app 18007

run_single 08 prompt-nacos examples/08-prompt-nacos-demo/pom.xml 18008 150 \
  curl_q http://localhost:18008/diagnosis 120 code=P0420

run_single 09 advisor examples/09-advisor-demo/pom.xml 18009 120 \
  curl_q http://localhost:18009/ask 120 question=hello

run_single 10 custom-advisor examples/10-custom-advisor-demo/pom.xml 18010 120 \
  curl_q http://localhost:18010/ask 120 question=check order status

run_single 11 tool examples/11-tool-demo/pom.xml 18011 120 \
  curl_q http://localhost:18011/tool/direct 90 city=Shanghai

run_single 12 dynamic-tool examples/12-dynamic-tool-demo/pom.xml 18012 120 \
  curl_q http://localhost:18012/tool/dynamic 120 "question=3+5" enableCalculator=true

run_single 13 http-tool examples/13-http-tool-demo/pom.xml 18013 120 \
  curl_q http://localhost:18013/ask 120 question=AAPL stock price

run_single 14 db-tool examples/14-db-tool-demo/pom.xml 18014 120 \
  curl_q http://localhost:18014/db/ask 120 question=list headphones

run_single 15 tool-security examples/15-tool-security-demo/pom.xml 18015 120 \
  curl_q http://localhost:18015/admin/ask 120 question=delete doc-001 role=ADMIN

info "Demo 16 memory (port 18016)"
if start_app examples/16-memory-demo/pom.xml 18016 16-memory 120 >/dev/null; then
  r1=$(curl_post_json http://localhost:18016/memory/chat '{"conversationId":"alice","message":"I am Alice"}' 120)
  parse_response "$r1"; ok16=$?
  r2=$(curl_post_json http://localhost:18016/memory/chat '{"conversationId":"alice","message":"What is my name?"}' 120)
  parse_response "$r2"
  if [ $ok16 -eq 0 ] && validate_ok "$BODY" "$HTTP_CODE"; then ok "16 memory — OK"; else bad "16 memory — 失败"; fi
else bad "16 memory — 启动失败"; fi
stop_app 18016

info "Demo 17 redis-memory (port 18017)"
if start_app examples/17-redis-memory-demo/pom.xml 18017 17-redis-memory 120 >/dev/null; then
  curl_q http://localhost:18017/chat 120 message=Alice userId=alice >/dev/null
  raw=$(curl_q http://localhost:18017/chat 120 message="what is my name" userId=alice)
  parse_response "$raw"
  if validate_ok "$BODY" "$HTTP_CODE"; then ok "17 redis-memory — OK"; else bad "17 redis-memory — HTTP ${HTTP_CODE}"; fi
else bad "17 redis-memory — 启动失败"; fi
stop_app 18017

info "Demo 18 jdbc-memory (port 18018)"
if start_app examples/18-jdbc-memory-demo/pom.xml 18018 18-jdbc-memory 150 >/dev/null; then
  curl_q http://localhost:18018/chat 120 message=Alice userId=alice >/dev/null
  raw=$(curl_q http://localhost:18018/chat 120 message="what is my name" userId=alice)
  parse_response "$raw"
  if validate_ok "$BODY" "$HTTP_CODE"; then ok "18 jdbc-memory — OK"; else bad "18 jdbc-memory — HTTP ${HTTP_CODE}"; fi
else bad "18 jdbc-memory — 启动失败"; fi
stop_app 18018

run_single 19 summary-memory examples/19-summary-memory-demo/pom.xml 18019 150 \
  curl_q http://localhost:18019/chat 120 message=round1 conversationId=demo

run_single 20 structured-output examples/20-structured-output-demo/pom.xml 18020 120 \
  curl_q http://localhost:18020/diagnose/structured 120 dtcCode=P0420

run_single 21 json-schema examples/21-json-schema-demo/pom.xml 18021 120 \
  curl_q http://localhost:18021/filmography 120 actors=TomHanks

run_single 22 embedding examples/22-embedding-demo/pom.xml 18022 120 \
  curl_q http://localhost:18022/embedding/benchmark 120 text=OTA failure analysis

run_multi_vec() {
  local num=$1 name=$2 pom=$3 port=$4 doc=$5 query=$6
  info "Demo ${num} ${name} (port ${port})"
  if start_app "$pom" "$port" "${num}-${name}" 180 >/dev/null; then
    curl_post_json "http://localhost:${port}/documents" "$doc" 60 >/dev/null
    raw=$(curl_q "http://localhost:${port}/search" 90 q="$query" topK=3)
    parse_response "$raw"
    if validate_ok "$BODY" "$HTTP_CODE"; then ok "${num} ${name} — OK"; else bad "${num} ${name} — HTTP ${HTTP_CODE}"; fi
  else bad "${num} ${name} — 启动失败"; fi
  stop_app "$port"
}

DOC='{"content":"OTA upgrade failure causes","metadata":{"department":"vehicle-diag"}}'
run_multi_vec 23 pgvector examples/23-pgvector-demo/pom.xml 18023 "$DOC" "upgrade failure"
run_multi_vec 24 milvus examples/24-milvus-demo/pom.xml 18024 "$DOC" "catalyst efficiency"
run_multi_vec 25 redis-vector examples/25-redis-vector-demo/pom.xml 18025 "$DOC" "password reset"
run_multi_vec 26 es-hybrid examples/26-es-hybrid-demo/pom.xml 18026 "$DOC" "P0420 catalyst"

info "Demo 27 rag (port 18027)"
if start_app examples/27-rag-demo/pom.xml 18027 27-rag 180 >/dev/null; then
  curl_post_empty http://localhost:18027/ingest 120 >/dev/null
  raw=$(curl_q http://localhost:18027/ask 120 question="OTA failure reason")
  parse_response "$raw"
  if validate_ok "$BODY" "$HTTP_CODE"; then ok "27 rag — OK"; else bad "27 rag — HTTP ${HTTP_CODE}"; fi
else bad "27 rag — 启动失败"; fi
stop_app 18027

info "Demo 28 advanced-rag (port 18028)"
if start_app examples/28-advanced-rag-demo/pom.xml 18028 28-advanced-rag 180 >/dev/null; then
  curl_post_empty http://localhost:18028/ingest 120 >/dev/null
  raw=$(curl_q http://localhost:18028/ask 120 question="upgrade failed")
  parse_response "$raw"
  if validate_ok "$BODY" "$HTTP_CODE"; then ok "28 advanced-rag — OK"; else bad "28 advanced-rag — HTTP ${HTTP_CODE}"; fi
else bad "28 advanced-rag — 启动失败"; fi
stop_app 18028

info "Demo 29 hybrid-rag (port 18029)"
if start_app examples/29-hybrid-rag-demo/pom.xml 18029 29-hybrid-rag 180 >/dev/null; then
  curl_post_empty http://localhost:18029/ingest 120 >/dev/null
  raw=$(curl_q http://localhost:18029/ask 120 question="OTA failure")
  parse_response "$raw"
  if validate_ok "$BODY" "$HTTP_CODE"; then ok "29 hybrid-rag — OK"; else bad "29 hybrid-rag — HTTP ${HTTP_CODE}"; fi
else bad "29 hybrid-rag — 启动失败"; fi
stop_app 18029

run_single 30 rag-eval examples/30-rag-eval-demo/pom.xml 18030 180 \
  curl_post_empty http://localhost:18030/eval/run 180

# 31+32
info "Demo 31+32 mcp-server/client"
kill_port 18031; kill_port 18032
log31="${LOG_DIR}/31-mcp-server.log"; : >"$log31"
mvn -f examples/31-mcp-server-demo/pom.xml spring-boot:run -DskipTests -q >>"$log31" 2>&1 &
if wait_started 18031 "$log31" 120; then
  log32="${LOG_DIR}/32-mcp-client.log"; : >"$log32"
  mvn -f examples/32-mcp-client-demo/pom.xml spring-boot:run -DskipTests -q >>"$log32" 2>&1 &
  if wait_started 18032 "$log32" 150; then
    raw=$(curl_q http://localhost:18032/ask 120 question="order SO20260704001 status")
    parse_response "$raw"
    if validate_ok "$BODY" "$HTTP_CODE"; then ok "31+32 mcp — OK"; else bad "31+32 mcp — HTTP ${HTTP_CODE}"; fi
  else bad "32 mcp-client — 启动失败"; fi
else bad "31 mcp-server — 启动失败"; fi
kill_port 18031; kill_port 18032

run_single 33 mcp-auth examples/33-mcp-auth-demo/pom.xml 18033 120 \
  curl -sS -w "\n%{http_code}" --max-time 30 -H "Authorization: Bearer demo-secret" http://localhost:18033/health

# 34
info "Demo 34 mcp-nacos"
kill_port 18034; kill_port 18134
log34s="${LOG_DIR}/34-server.log"; : >"$log34s"
mvn -f examples/34-mcp-nacos-demo/order-mcp-server/pom.xml spring-boot:run -DskipTests -q >>"$log34s" 2>&1 &
if wait_started 18034 "$log34s" 180; then
  sleep 15
  log34c="${LOG_DIR}/34-client.log"; : >"$log34c"
  mvn -f examples/34-mcp-nacos-demo/office-assistant-client/pom.xml spring-boot:run -DskipTests -q >>"$log34c" 2>&1 &
  if wait_started 18134 "$log34c" 180; then
    raw=$(curl_q http://localhost:18134/ask 120 question="order SO20260704001 status")
    parse_response "$raw"
    if validate_ok "$BODY" "$HTTP_CODE"; then ok "34 mcp-nacos — OK"; else bad "34 mcp-nacos — HTTP ${HTTP_CODE}"; fi
  else bad "34 client — 启动失败"; fi
else bad "34 server — 启动失败"; fi
kill_port 18034; kill_port 18134

run_single 35 agent examples/35-agent-demo/pom.xml 18035 180 \
  curl_q http://localhost:18035/agent/diagnose 180 query=P0420

run_single 36 agent-skills examples/36-agent-skills-demo/pom.xml 18036 180 \
  curl_q http://localhost:18036/agent/skills 180 query=P0420 diagnosis

info "Demo 37 agent-hitl"
if start_app examples/37-agent-hitl-demo/pom.xml 18037 37-hitl 180 >/dev/null; then
  raw=$(curl -sS -w "\n%{http_code}" --max-time 180 -X POST \
    --data-urlencode "query=请向商户A支付99元" "http://localhost:18037/hitl/start")
  parse_response "$raw"
  if echo "$BODY" | grep -q 'PENDING_APPROVAL'; then
    tid=$(echo "$BODY" | sed -n 's/.*"threadId":"\([^"]*\)".*/\1/p' | head -1)
    if [ -n "$tid" ]; then
      raw2=$(curl -sS -w "\n%{http_code}" --max-time 180 -X POST \
        "http://localhost:18037/hitl/approve?threadId=${tid}")
      parse_response "$raw2"
      if validate_ok "$BODY" "$HTTP_CODE" && echo "$BODY" | grep -q 'COMPLETED'; then
        ok "37 agent-hitl — OK"
      else
        bad "37 agent-hitl — approve 失败 HTTP ${HTTP_CODE} body=${BODY:0:200}"
      fi
    else bad "37 agent-hitl — 无 threadId"
    fi
  elif validate_ok "$BODY" "$HTTP_CODE" && echo "$BODY" | grep -q 'COMPLETED'; then
    ok "37 agent-hitl — 直接完成（模型未触发 HITL）"
  else
    bad "37 agent-hitl — start 未达预期 HTTP ${HTTP_CODE} body=${BODY:0:200}"
  fi
else bad "37 agent-hitl — 启动失败"; fi
stop_app 18037

run_single 38 workflow examples/38-workflow-demo/pom.xml 18038 180 \
  curl_q http://localhost:18038/workflow/run 180 question=P0420

run_single 39 graph-parallel examples/39-graph-parallel-demo/pom.xml 18039 180 \
  curl_q http://localhost:18039/graph/parallel 180 question=P0420

run_single 40 graph-saga examples/40-graph-saga-demo/pom.xml 18040 180 \
  curl_q http://localhost:18040/graph/saga 120 orderId=ORD-1 forceFail=false

run_single 41 multi-agent examples/41-multi-agent-demo/pom.xml 18041 180 \
  curl_q http://localhost:18041/multi/sequential 180 query=P0420

run_single 42 supervisor examples/42-supervisor-demo/pom.xml 18042 180 \
  curl_q http://localhost:18042/supervisor/chat 180 query=check schedule

# 43
info "Demo 43 a2a-nacos"
kill_port 18043; kill_port 18143
log43s="${LOG_DIR}/43-server.log"; : >"$log43s"
mvn -f examples/43-a2a-nacos-demo/inventory-a2a-server/pom.xml spring-boot:run -DskipTests -q >>"$log43s" 2>&1 &
if wait_started 18043 "$log43s" 180; then
  sleep 15
  log43c="${LOG_DIR}/43-client.log"; : >"$log43c"
  mvn -f examples/43-a2a-nacos-demo/office-a2a-client/pom.xml spring-boot:run -DskipTests -q >>"$log43c" 2>&1 &
  if wait_started 18143 "$log43c" 200; then
    raw=$(curl_q http://localhost:18143/a2a/inventory 180 query="SKU-001 stock")
    parse_response "$raw"
    if validate_ok "$BODY" "$HTTP_CODE"; then ok "43 a2a-nacos — OK"; else bad "43 a2a-nacos — HTTP ${HTTP_CODE}"; fi
  else bad "43 client — 启动失败"; fi
else bad "43 server — 启动失败"; fi
kill_port 18043; kill_port 18143

info "Demo 44 stream"
if start_app examples/44-stream-demo/pom.xml 18044 44-stream 150 >/dev/null; then
  raw=$(curl -sS -N --max-time 60 "http://localhost:18044/chat/stream-unified?message=hello" 2>/dev/null || true)
  if validate_sse "$raw"; then ok "44 stream — SSE OK"; else bad "44 stream — SSE 无效"; fi
else bad "44 stream — 启动失败"; fi
stop_app 18044

run_single 45 observability examples/45-observability-demo/pom.xml 18045 150 \
  curl_q http://localhost:18045/obs/chat 120 message=hello

run_single 46 logging examples/46-logging-demo/pom.xml 18046 150 \
  curl_q http://localhost:18046/log/chat 120 message=hello

run_single 47 routing examples/47-routing-demo/pom.xml 18047 150 \
  curl_q http://localhost:18047/route/ask 120 question=hello

run_single 48 fallback examples/48-fallback-demo/pom.xml 18048 150 \
  curl_q http://localhost:18048/fallback/chat 120 message=hello

info "========== UAT 汇总 =========="
printf "通过: \033[32m%d\033[0m / 失败: \033[31m%d\033[0m / 总计: 48\n" "$PASS" "$FAIL"
RESULT_FILE="${RESULTS_DIR}/uat-results-$(date +%Y%m%d-%H%M%S).txt"
{
  echo "Phase 3 Full UAT — $(date -Iseconds)"
  echo "PASS=$PASS FAIL=$FAIL"
  for f in "${FAILURES[@]+"${FAILURES[@]}"}"; do echo "FAIL: $f"; done
} >"$RESULT_FILE"
echo "结果: $RESULT_FILE"
[ "$FAIL" -eq 0 ] && exit 0 || exit 1
