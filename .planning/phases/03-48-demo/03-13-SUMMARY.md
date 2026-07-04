---
phase: 03-48-demo
plan: 13
subsystem: api
tags: [spring-ai, model-router, fallback, dashscope, deepseek, starter]

requires:
  - phase: 03-48-demo
    provides: saa-learning-starter ModelRouter/FallbackModelRouter 与 03-multi-model 双模型 yml 模式
provides:
  - examples/47-routing-demo 多模型路由 REST + 冒烟 IT
  - examples/48-fallback-demo reportFailure 降级与 /fallback/status
affects:
  - 03-14-PLAN.md batch 3 收尾验收

tech-stack:
  added: []
  patterns:
    - "ModelRouter.route() + 按次 ChatClient.builder(model)"
    - "forceFail 模拟主模型连续 reportFailure 触发 FallbackModelRouter 阈值"

key-files:
  created:
    - examples/47-routing-demo/pom.xml
    - examples/47-routing-demo/src/main/java/com/flywhl/saa/routing/RoutingController.java
    - examples/47-routing-demo/src/test/java/com/flywhl/saa/routing/RoutingDemoApplicationIT.java
    - examples/48-fallback-demo/src/main/java/com/flywhl/saa/fallback/FallbackController.java
  modified: []

key-decisions:
  - "47 未实现教程 CostAwareRoutingPolicy 可选层，聚焦 starter ModelRouter 主路径"
  - "48 forceFail 通过对 dashScopeChatModel 连续 reportFailure(3) 对齐 FallbackModelRouterTest"

patterns-established:
  - "双模型 Demo：dashscope + deepseek starter + saa-learning-starter，Bean 名 dashScopeChatModel/deepSeekChatModel"
  - "降级状态 DTO 同包 record FallbackStatus，/fallback/status 仅暴露布尔"

requirements-completed: [REQ-phase-3-demos]

duration: 8min
completed: 2026-07-04
---

# Phase 03 Plan 13: Routing + Fallback Demos Summary

**starter ModelRouter 驱动的 47/48 双 Demo：DashScope 主备 DeepSeek 路由与 reportFailure 熔断状态可 curl 演示**

## Performance

- **Duration:** ~8 min
- **Started:** 2026-07-04T15:51:00Z
- **Completed:** 2026-07-04T15:59:29Z
- **Tasks:** 2
- **Files modified:** 14

## Accomplishments

- 新建 `47-routing-demo`（18047）：`ModelRouter.route()` + `AuditLoggingAdvisor`，含 `@EnabledIfEnvironmentVariable` 冒烟 IT
- 新建 `48-fallback-demo`（18048）：`/fallback/chat`（catch + reportFailure 重试）、`forceFail` 模拟熔断、`/fallback/status` 返回 `fallbackActive`
- 两模块均依赖 `saa-learning-starter`，未自写 `ModelRouter`/`FallbackModelRouter` 实现

## Task Commits

1. **Task 1: 新建 47-routing-demo** - `3bac95e` (feat)
2. **Task 2: 新建 48-fallback-demo** - `88b5144` (feat)

**Plan metadata:** pending (docs commit)

## Files Created/Modified

- `examples/47-routing-demo/**` - 路由问答 `/route/ask`、双模型 yml、冒烟 IT
- `examples/48-fallback-demo/**` - 降级 chat/status、forceFail 演示路径

## Decisions Made

- 跳过可选 `CostAwareRoutingPolicy` 装配层，避免与仅有两路 vendor 模型的 yml 产生教程 qwen 三档不一致
- `FallbackStatus` record 放在主包 `com.flywhl.saa.fallback`（D-08b 小 DTO 同包）

## Deviations from Plan

None - plan executed exactly as written.

## Issues Encountered

None

## User Setup Required

- `AI_DASHSCOPE_API_KEY`：47 IT 与两 Demo 主模型路径
- `DEEPSEEK_API_KEY`：48 `forceFail=true` 完整降级演示

## Next Phase Readiness

- 47/48 compile 绿，可供 03-14 batch 3 全量 compile 门禁
- 真机 curl / IT 需上述环境变量

## Self-Check: PASSED

- FOUND: examples/47-routing-demo/pom.xml
- FOUND: examples/48-fallback-demo/pom.xml
- FOUND: examples/47-routing-demo/src/test/java/com/flywhl/saa/routing/RoutingDemoApplicationIT.java
- FOUND: commit 3bac95e
- FOUND: commit 88b5144

---
*Phase: 03-48-demo*
*Completed: 2026-07-04*
