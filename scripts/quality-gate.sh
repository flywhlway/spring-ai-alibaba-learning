#!/usr/bin/env bash
# quality-gate.sh —— 本地与 CI 共用的统一质量门禁入口（HANDOFF §7 / Phase 7 D-07）
# 失败时非零退出；不依赖 AI_DASHSCOPE_API_KEY；永不 echo 真实 API Key。
set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT"

# spring-ai-2-readiness 基线（仅超过时失败；勿设为 0）
# 锁定于 2026-07-17 Phase 07-01：Jackson / MCP / .withXxx 文件数
BASELINE_JACKSON_FILES=43
BASELINE_MCP_FILES=10
BASELINE_WITH_XXX_FILES=29

echo "== quality-gate: 1/6 common + starter clean install =="
mvn -B -pl common,starter -am clean install

echo "== quality-gate: 2/6 examples 编译抽样 =="
mvn -B -f examples/01-quickstart-demo/pom.xml -DskipTests compile
mvn -B -f examples/35-agent-demo/pom.xml -DskipTests compile

echo "== quality-gate: 3/6 version-audit =="
bash scripts/version-audit.sh

echo "== quality-gate: 4/6 父 POM 双 BOM 二次断言 =="
if ! grep -q "spring-ai-alibaba-bom" pom.xml; then
  echo "[失败] 父 POM 缺少 spring-ai-alibaba-bom"
  exit 1
fi
if ! grep -q "spring-ai-alibaba-extensions-bom" pom.xml; then
  echo "[失败] 父 POM 缺少 spring-ai-alibaba-extensions-bom"
  exit 1
fi
echo "  [OK] 双 BOM 仍存在"

echo "== quality-gate: 5/6 spring-ai-2-readiness（基线 ${BASELINE_JACKSON_FILES},${BASELINE_MCP_FILES},${BASELINE_WITH_XXX_FILES}）=="
bash scripts/spring-ai-2-readiness.sh . \
  --fail-above "${BASELINE_JACKSON_FILES},${BASELINE_MCP_FILES},${BASELINE_WITH_XXX_FILES}"

echo "== quality-gate: 6/6 HANDOFF §7 扫描 =="

SCAN_DIRS=(common starter examples projects)
JAVA_FILES=()
for d in "${SCAN_DIRS[@]}"; do
  if [[ -d "$d" ]]; then
    while IFS= read -r -d '' f; do
      JAVA_FILES+=("$f")
    done < <(find "$d" -type f -name '*.java' \
      ! -path '*/target/*' ! -path '*/.git/*' ! -path '*/node_modules/*' -print0 2>/dev/null)
  fi
done

# 废弃 API
DEPRECATED_HITS=$(printf '%s\0' "${JAVA_FILES[@]}" \
  | xargs -0 grep -nE 'PromptChatMemoryAdvisor|CallAroundAdvisor|AdvisedRequest|AdvisedResponse|FunctionCallback' \
    2>/dev/null || true)
if [[ -n "${DEPRECATED_HITS}" ]]; then
  echo "[失败] 发现废弃 API："
  echo "${DEPRECATED_HITS}"
  exit 1
fi
echo "  [OK] 无废弃 API"

# TODO / FIXME / 请自行补充（词边界，避免 mapToDouble 等假阳性）
TODO_HITS=$(printf '%s\0' "${JAVA_FILES[@]}" \
  | xargs -0 grep -nE '\bTODO\b|\bFIXME\b|请自行补充' 2>/dev/null || true)
if [[ -n "${TODO_HITS}" ]]; then
  echo "[失败] 发现 TODO/伪代码："
  echo "${TODO_HITS}"
  exit 1
fi
echo "  [OK] 无 TODO/FIXME/请自行补充"

# 硬编码密钥 sk-...；排除 example 模板与构建产物
SECRET_HITS=$(find common starter examples projects scripts \
  \( -name '*.java' -o -name '*.yml' -o -name '*.yaml' -o -name '*.properties' -o -name '*.sh' \) \
  ! -path '*/target/*' ! -path '*/.git/*' ! -path '*/node_modules/*' \
  ! -path 'scripts/setup-env.example.sh' \
  -print0 2>/dev/null \
  | xargs -0 grep -nE 'sk-[A-Za-z0-9]{16,}' 2>/dev/null || true)
if [[ -n "${SECRET_HITS}" ]]; then
  echo "[失败] 发现疑似硬编码密钥（sk-）："
  echo "${SECRET_HITS}"
  exit 1
fi
echo "  [OK] 无硬编码 sk- 密钥"

echo "quality-gate OK"
