#!/usr/bin/env bash
# =====================================================================
# deploy-smoke.sh —— Compose 类生产部署 smoke 骨架（Phase 7 / D-10）
#
# 教学仓「一键起中间件 + 打包 + 健康检查」骨架；不引入 K8s/Helm（D-11）。
#
# 前置：
#   - 本机 Docker / OrbStack 可用
#   - 真机模型调用另需 AI_DASHSCOPE_API_KEY（source scripts/setup-env*.sh）
#   - 演示口令（admin/admin123 等）仅限本机，生产必须替换（见各项目 README）
#
# 用法：
#   bash scripts/deploy-smoke.sh kqa              # 知识库问答 · 19100
#   bash scripts/deploy-smoke.sh office           # 办公助手 · 19200
#   bash scripts/deploy-smoke.sh smartcs          # 智能客服 · 19300
#   bash scripts/deploy-smoke.sh knowledge-qa-platform
#   bash scripts/deploy-smoke.sh kqa --skip-infra # 跳过中间件（已手动 up）
#   bash scripts/deploy-smoke.sh kqa --no-start   # 只 package + 提示，不后台起应用
#
# 等价 compose（本脚本内部复用，勿另写编排）：
#   docker compose -f docker/docker-compose.yml \
#     -f projects/<proj>/docker-compose.override.yml \
#     --profile ... up -d
# =====================================================================
set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT"

COMPOSE_BASE="docker/docker-compose.yml"
WAIT_MAX_SEC="${DEPLOY_SMOKE_WAIT_SEC:-90}"
WAIT_INTERVAL_SEC=5
SKIP_INFRA=0
NO_START=0
PROJECT_ARG=""

usage() {
  sed -n '2,30p' "$0" | sed 's/^# \{0,1\}//'
  exit 1
}

while [[ $# -gt 0 ]]; do
  case "$1" in
    -h|--help) usage ;;
    --skip-infra) SKIP_INFRA=1; shift ;;
    --no-start) NO_START=1; shift ;;
    -*)
      echo "[失败] 未知参数: $1" >&2
      usage
      ;;
    *)
      if [[ -n "$PROJECT_ARG" ]]; then
        echo "[失败] 只能指定一个项目: 已有 $PROJECT_ARG，又收到 $1" >&2
        exit 1
      fi
      PROJECT_ARG=$1
      shift
      ;;
  esac
done

[[ -n "$PROJECT_ARG" ]] || usage

# 别名 → 目录名 / 端口 / compose profiles（与各 projects/*/README.md 对齐）
resolve_project() {
  local key=$1
  case "$key" in
    kqa|knowledge-qa-platform)
      PROJ_DIR="knowledge-qa-platform"
      PORT=19100
      PROFILES=(core vector cloud kqa)
      NEEDS_MILVUS=1
      ;;
    office|office-agent-assistant)
      PROJ_DIR="office-agent-assistant"
      PORT=19200
      PROFILES=(core office)
      NEEDS_MILVUS=0
      ;;
    smartcs|smart-cs|smart-cs-platform)
      PROJ_DIR="smart-cs-platform"
      PORT=19300
      PROFILES=(core vector search cloud smartcs)
      NEEDS_MILVUS=1
      ;;
    *)
      echo "[失败] 未知项目: $key（支持 kqa|office|smartcs 或目录名）" >&2
      exit 1
      ;;
  esac
  OVERRIDE="projects/${PROJ_DIR}/docker-compose.override.yml"
  POM="projects/${PROJ_DIR}/pom.xml"
  JAR_GLOB="projects/${PROJ_DIR}/target/${PROJ_DIR}*.jar"
}

resolve_project "$PROJECT_ARG"

echo "== deploy-smoke: 项目=${PROJ_DIR} 端口=${PORT} =="
echo "   警告: 演示口令须替换后再用于任何非本机环境；密钥仅经 AI_DASHSCOPE_API_KEY 等环境变量注入。"

# 组装 docker compose 参数到全局 COMPOSE_ARGS（兼容 macOS Bash 3.2，无 mapfile）
build_compose_args() {
  COMPOSE_ARGS=(-f "$COMPOSE_BASE" -f "$OVERRIDE")
  local p
  for p in "${PROFILES[@]}"; do
    COMPOSE_ARGS+=(--profile "$p")
  done
}

# ---------- 1) 中间件 ----------
if [[ "$SKIP_INFRA" -eq 0 ]]; then
  echo "== deploy-smoke: 1/4 拉起中间件（docker compose + override）=="
  # 复用仓库既有 compose 路径；infra.sh 适用于无 override 的通用 profile，
  # 企业项目必须叠加 docker-compose.override.yml（建库/bucket/redis-stack 等）。
  build_compose_args
  docker compose "${COMPOSE_ARGS[@]}" up -d
  # 通用 core 也可经 infra.sh（无项目 override 时）：bash scripts/infra.sh up core
else
  echo "== deploy-smoke: 1/4 跳过中间件（--skip-infra）=="
fi

# ---------- 2) 等待 healthy（Milvus 冷启动约 30~60s）----------
echo "== deploy-smoke: 2/4 等待依赖健康（最长 ${WAIT_MAX_SEC}s）=="
elapsed=0
build_compose_args
while true; do
  # docker compose ps 退出码对部分 unhealthy 仍为 0；用输出扫描
  ps_out=$(docker compose "${COMPOSE_ARGS[@]}" ps 2>/dev/null || true)
  unhealthy=$(echo "$ps_out" | grep -ciE 'unhealthy|starting|created' || true)
  if [[ "$unhealthy" -eq 0 ]] && echo "$ps_out" | grep -qiE 'healthy|running|up '; then
    echo "  [OK] compose 服务已就绪（elapsed=${elapsed}s）"
    break
  fi
  if [[ "$elapsed" -ge "$WAIT_MAX_SEC" ]]; then
    echo "[失败] 中间件在 ${WAIT_MAX_SEC}s 内未全部 healthy（Milvus 冷启动常见 30~60s）。" >&2
    echo "       请执行: docker compose ${COMPOSE_ARGS[*]} ps" >&2
    echo "       或增大 DEPLOY_SMOKE_WAIT_SEC 后重试。" >&2
    exit 1
  fi
  if [[ "$NEEDS_MILVUS" -eq 1 ]]; then
    echo "  …等待中（含 Milvus，已 ${elapsed}s / ${WAIT_MAX_SEC}s）"
  else
    echo "  …等待中（已 ${elapsed}s / ${WAIT_MAX_SEC}s）"
  fi
  sleep "$WAIT_INTERVAL_SEC"
  elapsed=$((elapsed + WAIT_INTERVAL_SEC))
done

# ---------- 3) 打包 ----------
echo "== deploy-smoke: 3/4 Maven package =="
mvn -B -pl common,starter -am clean install -DskipTests
mvn -B -f "$POM" -DskipTests package

# ---------- 4) 启动应用 + 健康检查 ----------
HEALTH_URL="http://localhost:${PORT}/actuator/health"
echo "== deploy-smoke: 4/4 应用健康检查 ${HEALTH_URL} =="

if [[ "$NO_START" -eq 1 ]]; then
  echo "  [--no-start] 已跳过后台启动。手动运行："
  echo "    java -jar ${JAR_GLOB}"
  echo "  或: mvn -f ${POM} spring-boot:run"
  echo "  然后: curl -sf ${HEALTH_URL}"
  exit 0
fi

# 若端口已有进程在听，直接 curl（支持先手动 spring-boot:run）
if curl -sf "$HEALTH_URL" >/dev/null 2>&1; then
  echo "  [OK] ${PORT} 已在响应 /actuator/health（复用已启动实例）"
  curl -sf "$HEALTH_URL"
  echo
  exit 0
fi

shopt -s nullglob
jars=( $JAR_GLOB )
shopt -u nullglob
if [[ ${#jars[@]} -eq 0 ]]; then
  echo "[失败] 未找到 jar: ${JAR_GLOB}" >&2
  exit 1
fi
JAR="${jars[0]}"
LOG_FILE="/tmp/deploy-smoke-${PROJ_DIR}.log"
echo "  后台启动: java -jar ${JAR}  (日志: ${LOG_FILE})"
# 演示口令与 API Key 必须来自环境；切勿写入本脚本
nohup java -jar "$JAR" >"$LOG_FILE" 2>&1 &
APP_PID=$!
echo "  PID=${APP_PID}"

app_elapsed=0
APP_WAIT_SEC="${DEPLOY_SMOKE_APP_WAIT_SEC:-90}"
while true; do
  if curl -sf "$HEALTH_URL" >/dev/null 2>&1; then
    echo "  [OK] /actuator/health 通过（ports 19100/19200/19300 按项目）"
    curl -sf "$HEALTH_URL"
    echo
    exit 0
  fi
  if ! kill -0 "$APP_PID" 2>/dev/null; then
    echo "[失败] 应用进程已退出，见 ${LOG_FILE}" >&2
    tail -n 40 "$LOG_FILE" >&2 || true
    exit 1
  fi
  if [[ "$app_elapsed" -ge "$APP_WAIT_SEC" ]]; then
    echo "[失败] ${APP_WAIT_SEC}s 内 ${HEALTH_URL} 未就绪。日志: ${LOG_FILE}" >&2
    tail -n 40 "$LOG_FILE" >&2 || true
    exit 1
  fi
  sleep "$WAIT_INTERVAL_SEC"
  app_elapsed=$((app_elapsed + WAIT_INTERVAL_SEC))
done
