---
status: partial
phase: 06-smart-cs-platform
source: [06-VERIFICATION.md]
started: 2026-07-17T14:45:00Z
updated: 2026-07-17T14:45:00Z
---

## Current Test

[awaiting human testing]

## Tests

### 1. 健康检查与三角色登录
expected: health UP；admin/agent1/customer1 均 code=0 且拿到 accessToken；角色 claim 正确
result: [pending]

### 2. FAQ ask + SSE stream
expected: ask 返回答案；stream 含 message/done（FAQ 路径可出现 cacheHit）；cs_message 持久化
result: [pending]

### 3. 工单流转 + HITL handoff start/approve
expected: 非法 transition 400；approve 后工单 HUMAN_HANDLING，HITL resume 成功
result: [pending]

### 4. 运营看板 + Nacos + 监控
expected: stats 含会话/工单/cacheHitRate/成本字段；Nacos 出现 scs.model.profiles 与 prompt Data ID；prometheus 可 scrape
result: [pending]

### 5. uat-smart-cs.sh smoke
expected: 脚本 exit 0；与 06-UAT.md 预期一致
result: [pending]

## Summary

total: 5
passed: 0
issues: 0
pending: 5
skipped: 0
blocked: 0

## Gaps
