#!/usr/bin/env bash
# =====================================================================
# 中间件生命周期辅助脚本（封装 docker/docker-compose.yml 的常用组合）
#
# 用法：
#   bash scripts/infra.sh up core            # 启动核心中间件
#   bash scripts/infra.sh up core vector     # 启动核心 + Milvus（RAG 全链路）
#   bash scripts/infra.sh down               # 停止全部（保留数据卷）
#   bash scripts/infra.sh clean              # 停止全部并删除数据卷（不可恢复）
#   bash scripts/infra.sh ps                 # 查看状态
# =====================================================================
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
COMPOSE_FILE="${SCRIPT_DIR}/../docker/docker-compose.yml"
ALL_PROFILES=(core vector mq search cloud)

usage() {
  grep '^#' "$0" | sed 's/^# \{0,1\}//'
  exit 1
}

[ $# -ge 1 ] || usage
CMD=$1
shift || true

case "$CMD" in
  up)
    [ $# -ge 1 ] || { echo "请指定至少一个 profile：${ALL_PROFILES[*]}"; exit 1; }
    ARGS=()
    for p in "$@"; do ARGS+=(--profile "$p"); done
    docker compose -f "$COMPOSE_FILE" "${ARGS[@]}" up -d
    docker compose -f "$COMPOSE_FILE" "${ARGS[@]}" ps
    ;;
  down)
    ARGS=()
    for p in "${ALL_PROFILES[@]}"; do ARGS+=(--profile "$p"); done
    docker compose -f "$COMPOSE_FILE" "${ARGS[@]}" down
    ;;
  clean)
    read -r -p "将删除全部中间件数据卷，确认？(yes/no) " ANSWER
    if [ "$ANSWER" = "yes" ]; then
      ARGS=()
      for p in "${ALL_PROFILES[@]}"; do ARGS+=(--profile "$p"); done
      docker compose -f "$COMPOSE_FILE" "${ARGS[@]}" down -v
    else
      echo "已取消"
    fi
    ;;
  ps)
    ARGS=()
    for p in "${ALL_PROFILES[@]}"; do ARGS+=(--profile "$p"); done
    docker compose -f "$COMPOSE_FILE" "${ARGS[@]}" ps
    ;;
  *)
    usage
    ;;
esac
