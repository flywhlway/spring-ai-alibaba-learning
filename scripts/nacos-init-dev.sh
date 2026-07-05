#!/usr/bin/env bash
# Nacos 3.x 开发环境：确保默认 nacos 用户存在（2.4+ 不再预置密码）
# 用法：bash scripts/nacos-init-dev.sh
set -euo pipefail

NACOS_ADDR="${NACOS_ADDR:-127.0.0.1:8848}"
IDENTITY_KEY="${NACOS_AUTH_IDENTITY_KEY:-serverIdentity}"
IDENTITY_VALUE="${NACOS_AUTH_IDENTITY_VALUE:-saa-learning-dev}"
DEV_USER="${NACOS_DEV_USERNAME:-nacos}"
DEV_PASS="${NACOS_DEV_PASSWORD:-nacos}"

wait_nacos() {
  local i=0
  while [ "$i" -lt 30 ]; do
    if curl -sf --max-time 3 "http://${NACOS_ADDR}/nacos/v2/console/health/readiness" >/dev/null 2>&1 \
      || curl -sf --max-time 3 "http://127.0.0.1:8080/v3/console/health/readiness" >/dev/null 2>&1; then
      return 0
    fi
    # 8848 端口在监听也算就绪（readiness 路径因版本而异）
    if lsof -iTCP:"${NACOS_ADDR##*:}" -sTCP:LISTEN >/dev/null 2>&1; then
      return 0
    fi
    sleep 2
    i=$((i + 1))
  done
  echo "Nacos 未就绪: ${NACOS_ADDR}" >&2
  return 1
}

info() { printf '[nacos-init] %s\n' "$*"; }

wait_nacos

# 已可登录则跳过
if curl -sf --max-time 5 -X POST "http://${NACOS_ADDR}/nacos/v3/auth/user/login" \
  -H 'Content-Type: application/x-www-form-urlencoded' \
  -d "username=${DEV_USER}&password=${DEV_PASS}" | grep -q accessToken; then
  info "用户 ${DEV_USER} 已存在，跳过"
  exit 0
fi

info "创建开发用户 ${DEV_USER}（identity: ${IDENTITY_KEY}）"
resp=$(curl -sS --max-time 10 -X POST "http://${NACOS_ADDR}/nacos/v3/auth/user" \
  -H "${IDENTITY_KEY}: ${IDENTITY_VALUE}" \
  -H 'Content-Type: application/x-www-form-urlencoded' \
  -d "username=${DEV_USER}&password=${DEV_PASS}")

if echo "$resp" | grep -qE '"code"\s*:\s*0|"create user ok"'; then
  info "用户创建成功"
  exit 0
fi

if echo "$resp" | grep -qi 'already exist'; then
  info "用户已存在"
  exit 0
fi

echo "创建用户失败: $resp" >&2
exit 1
