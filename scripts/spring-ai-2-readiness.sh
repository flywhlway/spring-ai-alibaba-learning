#!/usr/bin/env bash
# spring-ai-2-readiness.sh —— 扫描 2.0 迁移时需要改动的已知破坏点，量化迁移工作量
# 对应教程第 22 章《Spring AI 2.0 现状与迁移前瞻》可运行 Demo。
set -euo pipefail
SRC="${1:-.}"

echo "== 扫描 Jackson 2 旧包引用（2.0 将迁移到 tools.jackson）=="
grep -rEl "com\.fasterxml\.jackson" "$SRC" --include="*.java" 2>/dev/null | wc -l | xargs echo "  涉及文件数："

echo "== 扫描 MCP 社区包引用（2.0 迁移到 org.springframework.ai.mcp.annotation）=="
grep -rEl "org\.springaicommunity\.mcp" "$SRC" --include="*.java" 2>/dev/null | wc -l | xargs echo "  涉及文件数："

echo "== 扫描 Options 可变 setter 写法（2.0 仅保留 Builder）=="
grep -rEc "\.with[A-Z][a-zA-Z]+\(" "$SRC" --include="*.java" 2>/dev/null | grep -v ':0$' | wc -l | xargs echo "  含 .withXxx() 的文件数："

echo "== 提示：以上数字即为 2.0 迁移时的机械改动量级 =="
