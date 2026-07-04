---
phase: 03-48-demo
plan: 09
subsystem: agent
tags: [ReactAgent, SkillsAgentHook, ClasspathSkillRegistry, HumanInTheLoopHook, ModelCallLimitHook, MemorySaver, DashScope]

requires:
  - phase: 03-48-demo
    provides: Batch 3 plans + RESEARCH JAR API 映射
provides:
  - examples/35-agent-demo（ReactAgent + methodTools + 冒烟 IT）
  - examples/36-agent-skills-demo（ClasspathSkillRegistry + SkillsAgentHook）
  - examples/37-agent-hitl-demo（HITL start/approve REST）
affects: [03-10, 03-11, agent-framework demos]

tech-stack:
  added: [spring-ai-alibaba-agent-framework]
  patterns:
    - "methodTools + ModelCallLimitHook.runLimit 替代 .tools(Component)/.maxIterations"
    - "SkillsAgentHook + ClasspathSkillRegistry 替代 Skill.of"
    - "HumanInTheLoopHook.approvalOn + UUID threadId + addHumanFeedback/resume"

key-files:
  created:
    - examples/35-agent-demo/pom.xml
    - examples/35-agent-demo/src/main/java/com/flywhl/saa/agentdemo/VehicleDiagnosisAgentConfig.java
    - examples/35-agent-demo/src/test/java/com/flywhl/saa/agentdemo/AgentDemoApplicationIT.java
    - examples/36-agent-skills-demo/pom.xml
    - examples/36-agent-skills-demo/src/main/java/com/flywhl/saa/agentskills/SkillsAgentConfig.java
    - examples/36-agent-skills-demo/src/main/resources/skills/vehicle-diagnosis/SKILL.md
    - examples/37-agent-hitl-demo/pom.xml
    - examples/37-agent-hitl-demo/src/main/java/com/flywhl/saa/agenthitl/HitlController.java
    - examples/37-agent-hitl-demo/src/main/java/com/flywhl/saa/agenthitl/HitlAgentConfig.java
  modified: []

key-decisions:
  - "35/36/37 均不引入 saa-learning-starter（D-10）"
  - "36 用 ClasspathSkillRegistry(classpathPath=skills) 而非 FileSystemSkillRegistry fallback"
  - "37 threadId 使用 UUID；pending InterruptionMetadata 进程内 ConcurrentHashMap 暂存"

patterns-established:
  - "Pattern 1 ReactAgent：methodTools + ModelCallLimitHook + MemorySaver + Result"
  - "Skills：resources/skills/<name>/SKILL.md + SkillsAgentHook.hooks"
  - "HITL：invokeAndGetOutput → InterruptionMetadata → addHumanFeedback(APPROVED).resume()"

requirements-completed: [REQ-phase-3-demos]

duration: 5min
completed: 2026-07-04
---

# Phase 03 Plan 09: Agent demos 35~37 Summary

**ReactAgent / Skills / HITL 三 Demo 按 1.1.2.2 JAR 真源交付：methodTools、SkillsAgentHook、HumanInTheLoopHook.approvalOn，端口 18035~18037，common+Result，35 含 Key 门控冒烟 IT。**

## Performance

- **Duration:** 5 min
- **Started:** 2026-07-04T15:41:01Z
- **Completed:** 2026-07-04T15:46:22Z
- **Tasks:** 3/3
- **Files modified:** 28

## Accomplishments

- 35-agent-demo：车辆诊断 ReactAgent，`methodTools` + `ModelCallLimitHook(runLimit=6)` + `MemorySaver`，`GET /agent/diagnose`，冒烟 IT 受 `AI_DASHSCOPE_API_KEY` 门控
- 36-agent-skills-demo：`ClasspathSkillRegistry` 扫描 `skills/*/SKILL.md`，`SkillsAgentHook` 注入 `read_skill` / `SkillsInterceptor`
- 37-agent-hitl-demo：`approvalOn("execute_payment")`，`POST /hitl/start` → `POST /hitl/approve?threadId=`（UUID）

## Task Commits

1. **Task 1: 新建 35-agent-demo（ReactAgent + 冒烟 IT）** - `e8d226b` (feat)
2. **Task 2: 新建 36-agent-skills-demo（Skills Hook + Registry）** - `0815da6` (feat)
3. **Task 3: 新建 37-agent-hitl-demo（HITL 暂停/恢复）** - `e24a954` (feat)

## Files Created/Modified

- `examples/35-agent-demo/**` — ReactAgent 车辆诊断 + IT
- `examples/36-agent-skills-demo/**` — Skills 渐进式披露
- `examples/37-agent-hitl-demo/**` — HITL 支付审批 REST

## Decisions Made

- 包名按 PLAN/RESEARCH：`agentdemo` / `agentskills` / `agenthitl`（非简写 `agent`）
- 36 直接使用 `ClasspathSkillRegistry`（默认 classpath `skills`），未走 FileSystem fallback
- 37 pending 中断元数据仅存进程内 Map（D-15 无中间件）；重启后需重新 start

## Deviations from Plan

### Auto-fixed Issues

**1. [Rule 1 - Bug] Javadoc 中 `*/` 提前结束注释**
- **Found during:** Task 2（36-agent-skills-demo compile）
- **Issue:** `SkillsAgentConfig` 注释写了 `skills/*/SKILL.md`，`*/` 被当作 Javadoc 结束符，导致非法字符编译失败
- **Fix:** 改为「skills 下各技能目录的 SKILL.md」表述
- **Files modified:** `examples/36-agent-skills-demo/src/main/java/com/flywhl/saa/agentskills/SkillsAgentConfig.java`
- **Verification:** `mvn -f examples/36-agent-skills-demo/pom.xml -q compile` 退出码 0
- **Committed in:** `0815da6`

**Total deviations:** 1 auto-fixed (Rule 1)
**Impact on plan:** 无范围蔓延，仅修复编译阻塞。

## Issues Encountered

None beyond the Javadoc compile fix above.

## User Setup Required

真机 curl / 冒烟 IT 需设置 `AI_DASHSCOPE_API_KEY`；编译不依赖 Key。无中间件。

## Next Phase Readiness

Agent 能力域（35~37）可编译交付完成；后续 03-10（Graph 38~40）可复用 `MemorySaver` / agent-framework 依赖模式。共享 STATE/ROADMAP 交由 orchestrator 在并行 wave 结束后统一更新。

## Self-Check: PASSED

- FOUND: `examples/35-agent-demo/pom.xml`
- FOUND: `examples/36-agent-skills-demo/pom.xml`
- FOUND: `examples/37-agent-hitl-demo/pom.xml`
- FOUND: commit `e8d226b`
- FOUND: commit `0815da6`
- FOUND: commit `e24a954`
- FOUND: 三模块 `mvn -f ... compile` 全绿

---
*Phase: 03-48-demo*
*Completed: 2026-07-04*
