---
phase: 06-smart-cs-platform
plan: 05
subsystem: api
tags: [sse, ticket-state-machine, hitl, spring-security, react-agent]

requires:
  - phase: 06-smart-cs-platform/06-04
    provides: CsOrchestratorService + humanEscalationAgent + TicketService 最小实现
provides:
  - ChatController SSE 会话网关（message/meta/interrupt/done）
  - TicketService ALLOWED_TRANSITIONS 完整状态机 + TicketController
  - HumanHandoffController start/approve（addHumanFeedback + resume）
affects:
  - 06-06 admin/ops
  - 06-07 UAT

tech-stack:
  added: []
  patterns:
    - ALLOWED_TRANSITIONS EnumMap 服务端校验工单转移
    - SSE 分块 + interrupt 事件携带 threadId
    - HITL ConcurrentHashMap pending（演示）+ resume 恢复

key-files:
  created:
    - projects/smart-cs-platform/src/main/java/com/flywhl/saa/smartcs/controller/ChatController.java
    - projects/smart-cs-platform/src/main/java/com/flywhl/saa/smartcs/service/ChatService.java
    - projects/smart-cs-platform/src/main/java/com/flywhl/saa/smartcs/controller/TicketController.java
    - projects/smart-cs-platform/src/main/java/com/flywhl/saa/smartcs/controller/HumanHandoffController.java
    - projects/smart-cs-platform/src/main/java/com/flywhl/saa/smartcs/model/dto/TicketTransitionRequest.java
    - projects/smart-cs-platform/src/main/java/com/flywhl/saa/smartcs/model/dto/HandoffStartRequest.java
    - projects/smart-cs-platform/src/test/java/com/flywhl/saa/smartcs/service/TicketServiceTest.java
  modified:
    - projects/smart-cs-platform/src/main/java/com/flywhl/saa/smartcs/service/TicketService.java
    - projects/smart-cs-platform/src/main/java/com/flywhl/saa/smartcs/repository/CsTicketRepository.java
    - projects/smart-cs-platform/src/main/java/com/flywhl/saa/smartcs/model/dto/ChatRequest.java

key-decisions:
  - "createOrEscalate 经 OPEN→AI_PROCESSING→PENDING_HUMAN 合法路径，禁止 OPEN→PENDING_HUMAN 直跳"
  - "HITL pending InterruptionMetadata 演示用 ConcurrentHashMap，生产需 Redis/DB 持久化"
  - "approve 同时支持 query threadId 与 JSON body，兼容计划契约与 api.http"

patterns-established:
  - "工单状态机：仅 TicketService.transition 可改 status，REST 无直写字段"
  - "会话 UUID：conversationId ≡ RunnableConfig.threadId ≡ handoff threadId"
  - "SSE 事件约定：message / meta(routeAgent,cacheHit) / interrupt(threadId) / done / error"

requirements-completed: [REQ-phase-6-smart-cs]

duration: 5min
completed: 2026-07-17
---

# Phase 06 Plan 05: 会话 SSE + 工单状态机 + HITL Summary

**Chat SSE 网关、ALLOWED_TRANSITIONS 工单状态机与 HumanHandoff start/approve（addHumanFeedback+resume）闭环**

## Performance

- **Duration:** 5 min
- **Started:** 2026-07-17T14:14:14Z
- **Completed:** 2026-07-17T14:19:03Z
- **Tasks:** 3
- **Files modified:** 10

## Accomplishments

- TicketService 完整状态机 + TicketServiceTest（合法/非法转移、createOrEscalate）
- ChatController/ChatService：SSE 流式与同步问答，消息落库含 route_agent/cache_hit/token
- HumanHandoffController：start 中断缓存、approve resume，工单 PENDING_HUMAN→HUMAN_HANDLING

## Task Commits

Each task was committed atomically:

1. **Task 1 (RED): TicketService 状态机测试** - `2b10689` (test)
2. **Task 1 (GREEN): TicketService + TicketController** - `c65ca99` (feat)
3. **Task 2: ChatController + ChatService SSE** - `6eddb72` (feat)
4. **Task 3: HumanHandoffController** - `599014d` (feat)

**Plan metadata:** `28811ab` (docs: complete plan)

## Files Created/Modified

- `service/TicketService.java` - ALLOWED_TRANSITIONS、SCS-yyyyMMdd 工单号、escalate/HITL 辅助方法
- `controller/TicketController.java` - POST/GET/PATCH transition（AGENT/ADMIN）
- `controller/ChatController.java` - `/api/chat/ask` + `/api/chat/stream` TEXT_EVENT_STREAM
- `service/ChatService.java` - ensureConversation、消息归档、编排委托、SSE 分块
- `controller/HumanHandoffController.java` - start/approve + addHumanFeedback/resume
- `TicketServiceTest.java` - 状态机单测

## Decisions Made

- createOrEscalate 走 OPEN→AI_PROCESSING→PENDING_HUMAN，与 ALLOWED_TRANSITIONS 对齐（非直跳）
- HITL pending 用 ConcurrentHashMap（类注释标明生产需持久化）
- approve 兼容 `?threadId=` 与 JSON body

## Deviations from Plan

### Auto-fixed Issues

**1. [Rule 2 - Missing Critical] createOrEscalate 经合法双跳路径**
- **Found during:** Task 1
- **Issue:** 计划文案写「OPEN→PENDING_HUMAN」，但 ALLOWED_TRANSITIONS 不含该边
- **Fix:** 实现为 OPEN→AI_PROCESSING→PENDING_HUMAN
- **Files modified:** TicketService.java
- **Verification:** TicketServiceTest.createOrEscalate 绿
- **Committed in:** c65ca99

**2. [Rule 2 - Missing Critical] question 长度校验**
- **Found during:** Task 2（威胁 T-06-SC）
- **Issue:** SSE/ask 输入需长度限制
- **Fix:** `@Size(max = 2000)` on ChatRequest.question 与 stream 参数
- **Files modified:** ChatRequest.java, ChatController.java
- **Verification:** compile 绿
- **Committed in:** 6eddb72

---

**Total deviations:** 2 auto-fixed (2 missing critical)
**Impact on plan:** 对齐状态机契约与威胁缓解，无范围膨胀

## Issues Encountered

None

## User Setup Required

SSE/真机验收需 `AI_DASHSCOPE_API_KEY`（`scripts/setup-env.sh`）。本 plan 单测不依赖模型密钥。

## Next Phase Readiness

- 会话/工单/HITL API 可供 06-06 admin/ops 与 06-07 UAT 验收
- 无阻塞项

## Self-Check: PASSED

- FOUND: ChatController.java, TicketService.java, HumanHandoffController.java, TicketServiceTest.java
- FOUND commits: 2b10689, c65ca99, 6eddb72, 599014d

---
*Phase: 06-smart-cs-platform*
*Completed: 2026-07-17*
