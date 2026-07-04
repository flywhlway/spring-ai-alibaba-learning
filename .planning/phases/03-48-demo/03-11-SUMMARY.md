---
phase: 03-48-demo
plan: 11
subsystem: api
tags: [multi-agent, flow-agent, supervisor, a2a, nacos, react-agent, agent-tool]

requires:
  - phase: 03-48-demo
    provides: common Result/GlobalExceptionHandler、35-agent-demo ReactAgent 模式
provides:
  - 41-multi-agent-demo 四模式 FlowAgent REST
  - 42-supervisor-demo ReactAgent+AgentTool 总控
  - 43-a2a-nacos-demo 双模块 A2A+Nacos
affects: [03-12, 03-14]

tech-stack:
  added: [spring-ai-alibaba-agent-framework, spring-ai-alibaba-starter-a2a-nacos]
  patterns: [FlowAgent invoke→OverAllState, AgentTool.create, A2aRemoteAgent+NacosAgentCardProvider]

key-files:
  created:
    - examples/41-multi-agent-demo/
    - examples/42-supervisor-demo/
    - examples/43-a2a-nacos-demo/
  modified: []

key-decisions:
  - "41 四模式一律 JAR 真 API：LlmRoutingAgent.subAgents、LoopAgent+CountLoopStrategy"
  - "42 无 SupervisorAgent，用 ReactAgent.tools(AgentTool.create(sub))"
  - "43 配置键 spring.ai.alibaba.a2a.nacos.*，Client agentCardProvider 非 nacosServiceName"

patterns-established:
  - "FlowAgent REST：invoke(query, RunnableConfig) → FlowStateExtractor 从 messages 取文本"
  - "A2A 双模块对齐 34：Server 注册 AgentCard，Client discovery.enabled + 先 Server 后 Client"

requirements-completed: [REQ-phase-3-demos]

duration: 3min
completed: 2026-07-04
---

# Phase 3 Plan 11: Multi-Agent 41~43 Summary

**FlowAgent 四模式 + ReactAgent/AgentTool Supervisor + A2A-Nacos 双模块，全部 JAR 真 API 可编译交付**

## Performance

- **Duration:** 3 min
- **Started:** 2026-07-04T15:51:12Z
- **Completed:** 2026-07-04T15:54:27Z
- **Tasks:** 3
- **Files modified:** 32

## Accomplishments

- 41：Sequential / Parallel / LlmRouting / Loop 四端点 + 冒烟 IT
- 42：calendar/email 子 Agent + officeSupervisor `AgentTool.create` 调度
- 43：inventory-a2a-server(18043) + office-a2a-client(18143)，A2A 配置键与 34 结构对齐

## Task Commits

Each task was committed atomically:

1. **Task 1: 新建 41-multi-agent-demo** - `9b3235d` (feat)
2. **Task 2: 新建 42-supervisor-demo** - `2a0839b` (feat)
3. **Task 3: 新建 43-a2a-nacos-demo** - `bec8a64` (feat)

## Files Created/Modified

- `examples/41-multi-agent-demo/` - 四模式 FlowAgent + MultiAgentDemoApplicationIT
- `examples/42-supervisor-demo/` - Supervisor ReactAgent + CalendarTools/EmailTools
- `examples/43-a2a-nacos-demo/` - 双模块 A2A Server/Client + README 手动验证清单

## Decisions Made

- 41/42/43 均不引入 saa-learning-starter（D-10 仅 44~48）
- 43 Server 暴露 `/health` 便于本地就绪检查（A2A JSON-RPC 由 starter 自动装配）
- 文本提取复用 HITL 模式：从 `OverAllState.messages` 逆序取最后 `AssistantMessage`

## Deviations from Plan

None - plan executed exactly as written.

## Issues Encountered

None

## User Setup Required

- 41/42：`AI_DASHSCOPE_API_KEY`（curl / 冒烟 IT）
- 43：额外 `bash scripts/infra.sh up cloud`（Nacos），先 Server 后 Client

## Next Phase Readiness

- Multi-Agent 域 41~43 齐备，可并行推进 03-12（44~46 Stream/Obs/Log）
- 43 真机验证依赖 Nacos cloud profile（文档门禁，无强制 IT）

## Self-Check: PASSED

- [x] examples/41-multi-agent-demo/pom.xml
- [x] examples/42-supervisor-demo/pom.xml
- [x] examples/43-a2a-nacos-demo/inventory-a2a-server/pom.xml
- [x] examples/43-a2a-nacos-demo/office-a2a-client/pom.xml
- [x] Commits 9b3235d, 2a0839b, bec8a64

---
*Phase: 03-48-demo*
*Completed: 2026-07-04*
