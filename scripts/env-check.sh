#!/usr/bin/env bash
# =====================================================================
# 开发环境自检脚本（macOS / Apple Silicon / OrbStack）
# 用法：bash scripts/env-check.sh
# =====================================================================
set -u

PASS=0
FAIL=0

ok()   { printf "  \033[32m✔\033[0m %s\n" "$1"; PASS=$((PASS+1)); }
bad()  { printf "  \033[31m✘\033[0m %s\n" "$1"; FAIL=$((FAIL+1)); }
info() { printf "\n\033[1m%s\033[0m\n" "$1"; }

info "1. JDK（要求 21）"
if command -v java >/dev/null 2>&1; then
  JAVA_VER=$(java -version 2>&1 | head -n1)
  if java -version 2>&1 | grep -Eq '"(21|2[2-9])'; then
    ok "java 已安装：${JAVA_VER}"
  else
    bad "java 版本过低（需要 21+）：${JAVA_VER}"
  fi
else
  bad "未找到 java，可执行：brew install --cask temurin@21"
fi

info "2. Maven（要求 3.9+）"
if command -v mvn >/dev/null 2>&1; then
  ok "mvn 已安装：$(mvn -version 2>/dev/null | head -n1)"
else
  bad "未找到 mvn，可执行：brew install maven"
fi

info "3. Docker / OrbStack"
if command -v docker >/dev/null 2>&1; then
  if docker info >/dev/null 2>&1; then
    ok "docker 守护进程运行中：$(docker --version)"
  else
    bad "docker 已安装但守护进程未运行，请启动 OrbStack"
  fi
  if docker compose version >/dev/null 2>&1; then
    ok "docker compose 可用：$(docker compose version --short 2>/dev/null)"
  else
    bad "docker compose 插件不可用"
  fi
else
  bad "未找到 docker，可执行：brew install orbstack"
fi

info "4. 模型 API Key 环境变量"
if [ -n "${AI_DASHSCOPE_API_KEY:-}" ]; then
  ok "AI_DASHSCOPE_API_KEY 已设置（长度 ${#AI_DASHSCOPE_API_KEY}）"
else
  bad "AI_DASHSCOPE_API_KEY 未设置，参考 scripts/setup-env.example.sh"
fi
if [ -n "${DEEPSEEK_API_KEY:-}" ]; then
  ok "DEEPSEEK_API_KEY 已设置（长度 ${#DEEPSEEK_API_KEY}）"
else
  bad "DEEPSEEK_API_KEY 未设置，参考 scripts/setup-env.example.sh"
fi

info "5. 常用端口占用检查（应处于空闲，或被本仓库 compose 占用）"
for p in 6379 5432 3306 9000 19530 8848; do
  if lsof -iTCP:"$p" -sTCP:LISTEN >/dev/null 2>&1; then
    OWNER=$(lsof -iTCP:"$p" -sTCP:LISTEN 2>/dev/null | awk 'NR==2{print $1}')
    ok "端口 $p 已被占用（进程：${OWNER}），若为本仓库中间件则正常"
  else
    ok "端口 $p 空闲"
  fi
done

printf "\n结果：\033[32m%d 通过\033[0m / \033[31m%d 待处理\033[0m\n" "$PASS" "$FAIL"
[ "$FAIL" -eq 0 ] && exit 0 || exit 1
