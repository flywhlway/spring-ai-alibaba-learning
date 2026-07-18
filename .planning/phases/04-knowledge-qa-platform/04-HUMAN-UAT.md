---
status: complete
phase: 04-knowledge-qa-platform
source:
  - 04-UAT.md
  - scripts/uat-knowledge-qa.sh
started: 2026-07-18T02:07:00Z
updated: 2026-07-18T02:15:33Z
scope: automated script UAT (--auto, replaces human)
---

## Current Test

[testing complete]

## Tests

### 1. 健康检查
expected: GET /actuator/health → status UP
result: pass

### 2. 三角色登录（admin / zhangsan）
expected: code=0，accessToken 非空
result: pass
note: "JWT 签发补齐显式 HS256（Security 6.5 默认 RS256 与 HMAC 不兼容）"

### 3. 同步问答 citations
expected: POST /api/qa/ask citations 非空（需 Key + Milvus）
result: pass

### 4. SSE stream
expected: event:message + event:done
result: pass
note: "中文 question 已 URL 编码"

### 5. RBAC
expected: EMPLOYEE 访问 /api/admin/users → 403
result: pass

## Summary

total: 5
passed: 5
issues: 0
pending: 0
skipped: 0
blocked: 0

## UAT Run

| Run | Date | Pass | Fail | Notes |
|-----|------|------|------|-------|
| auto-script | 2026-07-18 10:15 | 8 | 0 | `bash scripts/uat-knowledge-qa.sh` exit 0；含 Key 全量 |

## Hotfixes applied during UAT

1. `AiClientConfig` 显式 `@Bean ChatClient.Builder` — 双 ChatModel 下 ChatClientAutoConfiguration 启动失败
2. `query-rewrite` 模板补 `{target}`+`{query}`（DB seed + classpath `.st`）
3. JWT `JwsHeader` 显式 HS256
4. UAT 脚本 SSE query URL 编码
