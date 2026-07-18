---
phase: 06-smart-cs-platform
fixed_at: 2026-07-18T01:52:10Z
review_path: .planning/phases/06-smart-cs-platform/06-REVIEW.md
iteration: 1
findings_in_scope: 8
fixed: 8
skipped: 0
status: all_fixed
---

# Phase 06: Code Review Fix Report

**Fixed at:** 2026-07-18T01:52:10Z
**Source review:** `.planning/phases/06-smart-cs-platform/06-REVIEW.md`
**Iteration:** 1

**Summary:**
- Findings in scope: 8（CR-01..03 + WR-01..05；跳过 IN-*）
- Fixed: 8
- Skipped: 0

## Fixed Issues

### CR-01: Chat 路径触发 HITL 中断后无法 approve，且工单未升至 PENDING_HUMAN

**Files modified:** `HitlPendingStore.java`（新建）, `ChatService.java`, `HumanHandoffController.java`, `CsAgentConfig.java`, `uat-smart-cs.sh`
**Commit:** `c21046f`
**Applied fix:** 抽出共享 `HitlPendingStore`；`ChatService.ask/stream` 中断时注册 `InterruptionMetadata` 并调用 `transitionToPendingHuman`；`humanEscalationAgent` 与 `csIntentRouter` 共用 `MemorySaver`；UAT 取消 approve soft-pass，改为硬断言 200。
**Status note:** `fixed: requires human verification`（真机 HITL approve 需有 Key 的 UAT 复跑确认）

### CR-02: 工单查询/催单存在 IDOR

**Files modified:** `TicketController.java`, `TicketTools.java`
**Commit:** `162d938`
**Applied fix:** `GET /api/tickets/{no}` 对 CUSTOMER 校验 `customerId`；Tool 层 `queryTicketByNo`/`urgeTicket` 同样做归属校验，AGENT/ADMIN 放行。

### CR-03: Tool 身份回退为 CUSTOMER 且未注入 ToolContext

**Files modified:** `ToolSecuritySupport.java`, `CsOrchestratorService.java`, `ChatService.java`, `HumanHandoffController.java`, `TicketTools.java`, `HandoffTools.java`
**Commit:** `f168f4a`
**Applied fix:** 缺少身份时抛 `UNAUTHORIZED`（禁止默认 CUSTOMER）；编排/start/approve 经 `RunnableConfig.addMetadata(userId/role/conversationId)` 注入（框架会并入 ToolContext）；SSE 路径捕获并恢复 `SecurityContext`；Tool 优先使用服务端 `conversationId`。

### WR-01: 创建工单/发起接管未校验 conversation 归属

**Files modified:** `TicketController.java`, `HumanHandoffController.java`
**Commit:** `d20492e`
**Applied fix:** `POST /api/tickets` 与 `/api/handoff/start` 调用 `ChatService.ensureConversation`（CUSTOMER 仅本人会话）。

### WR-02: 工单号生成存在并发碰撞窗口

**Files modified:** `TicketService.java`, `TicketServiceTest.java`
**Commits:** `7041547`, `e9c5510`, `1287c65`
**Applied fix:** `ticket_no` UNIQUE 冲突时以 `REQUIRES_NEW` 新事务重试（最多 5 次）；单机 `synchronized` 收窄序号窗口；同步更新单测构造器与 `saveAndFlush` stub。
**Status note:** `fixed: requires human verification`（并发压力需人工或压测确认）

### WR-03: 多智能体路径未使用 ConfigurableModelRouter

**Files modified:** `CsAgentConfig.java`
**Commit:** `8d7da53`
**Applied fix:** 各 Agent 按 scene 调用 `routeForScene(FAQ/BUSINESS/TICKET)`；意图路由回退 `route()`。

### WR-04: SSE `/api/chat/stream` 将完整问题放在 query string

**Files modified:** `ChatController.java`, `http/api.http`, `scripts/uat-smart-cs.sh`, `README.md`
**Commit:** `a322724`
**Applied fix:** `GET` 改为 `POST /api/chat/stream` + `ChatRequest` JSON body；同步更新 api.http / UAT / README。

### WR-05: Dashboard Token 成本对 gen_ai 计数器一律按 input 单价估算

**Files modified:** `DashboardStatsService.java`
**Commit:** `1381a95`
**Applied fix:** 按 tag `gen_ai.token.type`（`input`/`output`）分别乘对应单价；跳过 `total`；无法识别时回退消息表估算。

## Skipped Issues

None — all in-scope findings were fixed.

## Out of scope (Info)

- IN-01 JWT/noop 开发默认值 — 未改
- IN-02 `/actuator/prometheus` permitAll — 未改
- IN-03 Redis pending 持久化 — 未做（CR-01 先落地进程内共享 store；生产外置仍属后续债）

---

_Fixed: 2026-07-18T01:52:10Z_
_Fixer: Claude (gsd-code-fixer)_
_Iteration: 1_
