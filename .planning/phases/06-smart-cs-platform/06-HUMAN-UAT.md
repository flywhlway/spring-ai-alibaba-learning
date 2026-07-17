---
status: partial
phase: 06-smart-cs-platform
source:
  - 06-VERIFICATION.md
  - 06-08-SUMMARY.md
  - 06-09-SUMMARY.md
started: 2026-07-17T14:45:00Z
updated: 2026-07-17T16:25:00Z
scope: post-gap-closure human retest
---

## Current Test

[awaiting human testing — 冷启动 blocker 已清除，需补跑全链路 UAT]

## Tests

### 1. 健康检查与三角色登录
expected: health UP；admin/agent1/customer1 均 code=0 且拿到 accessToken；角色 claim 正确
result: pending
note: "Gap 关闭后 orchestrator 已证 health 曾 UP；三角色 login 待人工复跑"

### 2. FAQ ask + SSE stream
expected: ask 返回答案；stream 含 message/done（FAQ 路径可出现 cacheHit）；cs_message 持久化
result: pending
note: "需 DashScope + 向量/检索栈；先前被冷启动挡住"

### 3. 工单流转 + HITL handoff start/approve
expected: 非法 transition 400；合法流转成功；approve 404 见 D-14 Pending（勿当新 gap）
result: pending
note: "CR-01 / D-14 另案 /gsd-code-review 6 --fix"

### 4. 运营看板 + Nacos + 监控
expected: stats 含会话/工单/cacheHitRate/成本字段；Nacos 出现 scs.model.profiles 与 prompt Data ID
result: pending

### 5. uat-smart-cs.sh smoke
expected: 脚本 exit 0；与 06-UAT.md 预期一致（HITL approve 已知 D-14 除外）
result: pending
note: "冷启动修复后应可达；验收：bash projects/smart-cs-platform/scripts/uat-smart-cs.sh"

## Summary

total: 5
passed: 0
issues: 0
pending: 5
skipped: 0
blocked: 0

## UAT Run

| Run | Date | Pass | Fail | Notes |
|-----|------|------|------|-------|
| v1 --auto | 2026-07-17 23:44~23:47 | 0 | startup | scs-db-init 挂载到 docker/db（空目录） |
| v1+manual-db | 2026-07-17 23:48~23:51 | 0 | startup | 缺 {target} |
| gap-closure | 2026-07-18 | — | — | 06-08/09 已修；health UP 已证；全链路待补跑 |

**验收命令：** `bash projects/smart-cs-platform/scripts/uat-smart-cs.sh`（需 Key + smartcs compose profiles）

## Gaps

- truth: "按文档 compose + smartcs profile 后，scs_platform 自动拥有 schema/data，应用可冷启动 health UP"
  status: resolved
  resolved_by: 06-08
  evidence: "volume ../projects/.../db；inspect Source 正确；init exit 0；health UP"
  debug_session: ".planning/debug/scs-db-init-volume.md"

- truth: "query-rewrite Prompt 满足 Spring AI RewriteQueryTransformer 必填占位符 {target}+{query}，RetrievalAugmentationAdvisor Bean 可创建"
  status: resolved
  resolved_by: 06-09
  evidence: "classpath+种子+幂等 UPDATE；boot 无 PromptAssert target 错误；health UP"
  debug_session: ".planning/debug/query-rewrite-target.md"
