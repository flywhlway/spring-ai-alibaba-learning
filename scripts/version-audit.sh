#!/usr/bin/env bash
# version-audit.sh —— 校验 SAA / Spring AI 版本是否在整个依赖树中唯一且对齐
# 对应教程第 21 章《版本升级指南》可运行 Demo。
set -euo pipefail

echo "== 1. 检查 SAA 是否存在多版本（应只有一个版本号）=="
mvn -q dependency:tree -Dincludes=com.alibaba.cloud.ai 2>/dev/null \
  | grep -oE 'spring-ai-alibaba[^:]*:jar:[0-9.]+' | sort -u || echo "  (未解析到 SAA 依赖，请确认已引入 starter)"

echo "== 2. 检查 Spring AI 主线是否唯一 =="
mvn -q dependency:tree -Dincludes=org.springframework.ai 2>/dev/null \
  | grep -oE 'spring-ai[^:]*:jar:[0-9.]+' | sort -u || echo "  (未解析到 Spring AI 依赖)"

echo "== 3. 检查是否误引入 2.0 线（当前 SAA 尚不兼容，见第22章）=="
if mvn -q dependency:tree -Dincludes=org.springframework.ai 2>/dev/null | grep -qE ':jar:2\.'; then
  echo "  [警告] 检测到 Spring AI 2.x，当前 SAA 版本线不兼容，请核对！"
  exit 1
else
  echo "  [OK] 未检测到 2.x，符合预期（本仓库锁定 1.1.2 线）"
fi

echo "== 4. 确认父 POM 同时导入两个 BOM =="
grep -q "spring-ai-alibaba-bom" pom.xml && echo "  [OK] 主 BOM 已导入" || echo "  [缺失] spring-ai-alibaba-bom"
grep -q "spring-ai-alibaba-extensions-bom" pom.xml && echo "  [OK] extensions BOM 已导入" || echo "  [缺失] extensions BOM"
