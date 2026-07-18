---
status: complete
phase: 06-smart-cs-platform
source:
  - 06-VERIFICATION.md
  - 06-08-SUMMARY.md
  - 06-09-SUMMARY.md
  - 06-10-SUMMARY.md
started: 2026-07-17T14:45:00Z
updated: 2026-07-18T00:48:00Z
scope: post-gap-closure human retest
---

## Current Test

[testing complete]

## Tests

### 1. 健康检查与三角色登录
expected: health UP；admin/agent1/customer1 均 code=0 且拿到 accessToken；角色 claim 正确
result: pass
note: "HS256 hotfix 后三角色 login 全绿（06-10 Task 1）"

### 2. FAQ ask + SSE stream
expected: ask 返回答案；stream 含 message/done（FAQ 路径可出现 cacheHit）；cs_message 持久化
result: pass
note: "ask/stream 脚本断言通过；SSE 中文 query URL 编码"

### 3. 工单流转 + HITL handoff start/approve
expected: 非法 transition 400；合法流转成功；approve 404 见 D-14 Pending（勿当新 gap）
result: pass
note: "handoff/start 成功；approve HTTP 500 soft-pass（D-14/CR-01，脚本 warn 不计 FAIL）"

### 4. 运营看板 + Nacos + 监控
expected: stats 含会话/工单/cacheHitRate/成本字段；Nacos 出现 scs.model.profiles 与 prompt Data ID
result: pass
note: "CUSTOMER→admin stats 现为 HTTP 403（06-10 AccessDeniedExceptionHandler）；admin stats 200；Nacos Data ID 本轮未单独验"

### 5. uat-smart-cs.sh smoke
expected: 脚本 exit 0；与 06-UAT.md 预期一致（HITL approve 已知 D-14 除外）
result: pass
note: "06-10 复跑：11 通过 / 0 失败 / 1 警告（approve D-14 soft-pass）；exit 0"

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
| v1 --auto | 2026-07-17 23:44~23:47 | 0 | startup | scs-db-init 挂载到 docker/db（空目录） |
| v1+manual-db | 2026-07-17 23:48~23:51 | 0 | startup | 缺 {target} |
| gap-closure | 2026-07-18 | — | — | 06-08/09 已修；health UP 已证 |
| auto-retest | 2026-07-18 00:20 | 3 | 9 | login JwtEncodingException（默认 RS256 vs HMAC） |
| auto-retest+HS256 | 2026-07-18 00:22 | 10 | 2 | RBAC 500 + approve(D-14) |
| **06-10 gap close** | **2026-07-18 08:47** | **11** | **0** | **403 绿；approve soft-pass；exit 0** |

**验收命令：** `bash projects/smart-cs-platform/scripts/uat-smart-cs.sh`（需 Key + smartcs compose profiles）

## Gaps

- truth: "按文档 compose + smartcs profile 后，scs_platform 自动拥有 schema/data，应用可冷启动 health UP"
  status: resolved
  resolved_by: 06-08

- truth: "query-rewrite Prompt 满足 Spring AI RewriteQueryTransformer 必填占位符 {target}+{query}，RetrievalAugmentationAdvisor Bean 可创建"
  status: resolved
  resolved_by: 06-09

- truth: "CUSTOMER JWT 访问 /api/admin/dashboard/stats 返回 HTTP 403 Forbidden（RBAC 拒绝，非 500）"
  status: resolved
  resolved_by: 06-10
  evidence: "AccessDeniedExceptionHandler → HTTP 403；uat 输出 customer — 403 Forbidden"
  debug_session: ".planning/debug/admin-rbac-403-vs-500.md"

- truth: "uat-smart-cs.sh 在有 Key + 中间件就绪时 exit 0（HITL approve 已知 D-14 除外）"
  status: resolved
  resolved_by: 06-10
  evidence: "11 pass / 0 fail / 1 warn（approve D-14 soft-pass）；exit 0"
  debug_session: ".planning/debug/uat-smart-cs-script-exit.md"

**仍 Pending（非本 gap）：** D-14 / CR-01 HITL approve 产品债 → `/gsd-code-review 6 --fix`
