---
status: complete
phase: 05-office-agent-assistant
source:
  - projects/office-agent-assistant/scripts/uat-office-agent.sh
  - projects/office-agent-assistant/README.md
started: 2026-07-18T02:16:00Z
updated: 2026-07-18T02:22:26Z
scope: automated script UAT (--auto, replaces human)
---

## Current Test

[testing complete]

## Tests

### 1. 健康检查与登录
expected: health UP；zhangsan/admin login code=0；/me 含 zhangsan
result: pass
note: "JWT 显式 HS256；MySQL 主库 @Primary（修复 vectorDataSource 抢占）"

### 2. Prompt 管理 + RBAC
expected: admin prompts 含 meeting-summary；EMPLOYEE → /api/admin/users 403
result: pass

### 3. 办公任务 / Agent / 审批（需 Key）
expected: meeting-summary / chat / approvals/review 均 code=0
result: pass

### 4. mvn test 门禁
expected: 单测 + Testcontainers IT 通过
result: pass

## Summary

total: 4
passed: 4
issues: 0
pending: 0
skipped: 0
blocked: 0

## UAT Run

| Run | Date | Pass | Fail | Notes |
|-----|------|------|------|-------|
| auto-script | 2026-07-18 10:22 | 12 | 0 | 增强版 `uat-office-agent.sh` exit 0；含 Key 全量 |

## Hotfixes applied during UAT

1. JWT `JwsHeader` 显式 HS256（同 Phase 4/6）
2. MCP Client 默认关闭（自连 :19200 冷启动鸡生蛋）
3. `DataSourceConfig`：MySQL `@Primary`，避免 pgvector DataSource 抢占 JPA
4. UAT 脚本增强：自启应用 + chat/tasks/approvals/RBAC
