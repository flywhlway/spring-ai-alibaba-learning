#!/usr/bin/env bash
# spring-ai-2-readiness.sh —— 扫描 2.0 迁移时需要改动的已知破坏点，量化迁移工作量
# 对应教程第 22 章《Spring AI 2.0 现状与迁移前瞻》可运行 Demo。
# 用法：
#   bash scripts/spring-ai-2-readiness.sh [SRC]
#   bash scripts/spring-ai-2-readiness.sh [SRC] --fail-above JACKSON,MCP,WITH
# 无 --fail-above 时仅报告（教学路径）；带阈值时超过任一基线则 exit 1。
set -euo pipefail

SRC="."
FAIL_ABOVE=""
while [[ $# -gt 0 ]]; do
  case "$1" in
    --fail-above)
      FAIL_ABOVE="${2:-}"
      shift 2
      ;;
    *)
      SRC="$1"
      shift
      ;;
  esac
done

echo "== 扫描 Jackson 2 旧包引用（2.0 将迁移到 tools.jackson）=="
JACKSON_COUNT=$(grep -rEl "com\.fasterxml\.jackson" "$SRC" --include="*.java" 2>/dev/null | wc -l | tr -d ' ')
echo "  涉及文件数：${JACKSON_COUNT}"

echo "== 扫描 MCP 社区包引用（2.0 迁移到 org.springframework.ai.mcp.annotation）=="
MCP_COUNT=$(grep -rEl "org\.springaicommunity\.mcp" "$SRC" --include="*.java" 2>/dev/null | wc -l | tr -d ' ')
echo "  涉及文件数：${MCP_COUNT}"

echo "== 扫描 Options 可变 setter 写法（2.0 仅保留 Builder）=="
WITH_COUNT=$(grep -rEc "\.with[A-Z][a-zA-Z]+\(" "$SRC" --include="*.java" 2>/dev/null | grep -v ':0$' | wc -l | tr -d ' ')
echo "  含 .withXxx() 的文件数：${WITH_COUNT}"

echo "== 提示：以上数字即为 2.0 迁移时的机械改动量级 =="

if [[ -n "$FAIL_ABOVE" ]]; then
  IFS=',' read -r MAX_JACKSON MAX_MCP MAX_WITH <<< "$FAIL_ABOVE"
  FAILED=0
  if [[ "${JACKSON_COUNT}" -gt "${MAX_JACKSON}" ]]; then
    echo "  [失败] Jackson 文件数 ${JACKSON_COUNT} 超过基线 ${MAX_JACKSON}"
    FAILED=1
  fi
  if [[ "${MCP_COUNT}" -gt "${MAX_MCP}" ]]; then
    echo "  [失败] MCP 文件数 ${MCP_COUNT} 超过基线 ${MAX_MCP}"
    FAILED=1
  fi
  if [[ "${WITH_COUNT}" -gt "${MAX_WITH}" ]]; then
    echo "  [失败] .withXxx 文件数 ${WITH_COUNT} 超过基线 ${MAX_WITH}"
    FAILED=1
  fi
  if [[ "$FAILED" -ne 0 ]]; then
    exit 1
  fi
  echo "  [OK] 三项计数均未超过基线 ${FAIL_ABOVE}"
fi
