---
phase: 03-48-demo
plan: 12
subsystem: api
tags: [sse, prometheus, micrometer, mdc, traceId, starter, AuditLoggingAdvisor, streaming, observability]

requires:
  - phase: 03-48-demo
    provides: common + starter 底座（D-10 强制依赖）
provides:
  - 44-stream-demo：统一 SSE message/error/done + AuditLoggingAdvisor
  - 45-observability-demo：/actuator/prometheus + starter 成本采集
  - 46-logging-demo：MDC traceId + AuditLoggingAdvisor 日志贯通
affects: [03-13-plan, 03-14-plan, batch3-uat]

tech-stack:
  added: [saa-learning-starter, micrometer-registry-prometheus, spring-boot-starter-actuator]
  patterns: [defaultAdvisors(AuditLoggingAdvisor), 统一 SSE 协议, TraceIdFilter+MDC, starter CostTrackingObservationHandler]

key-files:
  created:
    - examples/44-stream-demo/
    - examples/45-observability-demo/
    - examples/46-logging-demo/
  modified:
    - pom.xml

key-decisions:
  - "44~46 均显式 defaultAdvisors(AuditLoggingAdvisor)，Starter 不会自动挂 Advisor"
  - "45 禁止复制教程手写 CostTrackingObservationHandler，依赖 starter 自动装配"
  - "父 POM dependencyManagement 补充 saa-learning-starter 以支持 Demo 零版本号依赖"

patterns-established:
  - "Stream/Obs/Logging Demo 标准 starter 装配：注入 AuditLoggingAdvisor → builder.defaultAdvisors(audit).build()"
  - "SSE error 事件序列化 Result.fail，不回传堆栈（T-44-01）"

requirements-completed: [REQ-phase-3-demos]

duration: 18min
completed: 2026-07-04
---

# Phase 03 Plan 12: Stream + Observability + Logging (44~46) Summary

**三个 starter 驱动的 Demo：统一 SSE 流式协议、Prometheus 指标与 MDC traceId 审计日志贯通**

## Performance

- **Duration:** 18 min
- **Started:** 2026-07-04T15:55:00Z
- **Completed:** 2026-07-04T16:13:00Z
- **Tasks:** 3
- **Files modified:** 24

## Accomplishments
- 新建 `44-stream-demo`（18044）：`GET /chat/stream-unified` SSE 事件 `message`/`error`/`done`，复用 `AuditLoggingAdvisor`，含 `@EnabledIfEnvironmentVariable` 冒烟 IT
- 新建 `45-observability-demo`（18045）：Actuator 暴露 `/actuator/prometheus`，复用 starter `CostTrackingObservationHandler`
- 新建 `46-logging-demo`（18046）：`TraceIdFilter` + `%X{traceId}` 日志格式，业务日志与 AUDIT 共享 traceId

## Task Commits

1. **Task 1: 新建 44-stream-demo** - `86a7ca2` (feat)
2. **Task 2: 新建 45-observability-demo** - `a4908a1` (feat)
3. **Task 3: 新建 46-logging-demo** - `3f853df` (feat)

## Files Created/Modified
- `examples/44-stream-demo/` - SSE 统一协议 + StreamConfig/Controller + 冒烟 IT
- `examples/45-observability-demo/` - Prometheus + ObservabilityController
- `examples/46-logging-demo/` - TraceIdFilter + LoggingController
- `pom.xml` - 补充 `saa-learning-starter` dependencyManagement

## Decisions Made
- Starter 提供 Bean 但不自动挂 ChatClient，三个 Demo 均在 Config 中显式 `defaultAdvisors(auditLoggingAdvisor)`
- 45 的成本采集完全依赖 starter 自动装配，不在 Demo 内复制教程 `@Component` Handler

## Deviations from Plan

### Auto-fixed Issues

**1. [Rule 3 - Blocking] 父 POM 缺少 saa-learning-starter dependencyManagement**
- **Found during:** Task 1（44-stream-demo compile）
- **Issue:** `mvn -f examples/44-stream-demo/pom.xml compile` 报 version missing
- **Fix:** 在根 `pom.xml` dependencyManagement 增加 `saa-learning-starter`
- **Files modified:** `pom.xml`
- **Verification:** 三个 Demo 均 `compile` 退出码 0
- **Committed in:** `86a7ca2`（Task 1 commit）

---

**Total deviations:** 1 auto-fixed (1 blocking)
**Impact on plan:** 必要基建补齐，符合 D-06 零版本号约定，无范围蔓延

## Issues Encountered
None

## User Setup Required
- 真机 curl / 冒烟 IT 需 `AI_DASHSCOPE_API_KEY`
- 45 Prometheus 端点仅本机 Demo，生产须鉴权/网络隔离

## Next Phase Readiness
- Batch 3 前半（44~46）可独立 `mvn spring-boot:run` 验收
- 后续 plan 13/14 可继续 47~48 或 Batch 3 剩余 Demo

## Self-Check: PASSED
- FOUND: examples/44-stream-demo/pom.xml
- FOUND: examples/45-observability-demo/pom.xml
- FOUND: examples/46-logging-demo/pom.xml
- FOUND: commit 86a7ca2
- FOUND: commit a4908a1
- FOUND: commit 3f853df

---
*Phase: 03-48-demo*
*Completed: 2026-07-04*
