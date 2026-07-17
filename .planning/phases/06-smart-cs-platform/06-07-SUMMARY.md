---
phase: 06-smart-cs-platform
plan: 07
subsystem: testing
tags: [testcontainers, mockmvc, uat, junit, handoff-gates]

requires:
  - phase: 06-smart-cs-platform
    provides: Auth/Chat/Ticket/Handoff/Admin 业务实现（06-01~06-06）
provides:
  - Testcontainers IT 基座（PG scs_platform + Redis）
  - 无 Key 可绿单测与冒烟测试
  - Auth/Chat/Ticket IT + API Key 门控 Model IT
  - uat-smart-cs.sh 与 06-UAT.md 验收清单
affects: [gsd-verify-work, phase-06-closeout]

tech-stack:
  added: [testcontainers junit-jupiter/postgresql, spring-boot-testcontainers]
  patterns: [ScsPostgresRedisITBase + DockerAvailableCondition, @MockBean 屏蔽向量库/Agent, AI_DASHSCOPE_API_KEY 门控]

key-files:
  created:
    - projects/smart-cs-platform/src/test/java/com/flywhl/saa/smartcs/support/ScsPostgresRedisITBase.java
    - projects/smart-cs-platform/src/test/java/com/flywhl/saa/smartcs/support/DockerAvailableCondition.java
    - projects/smart-cs-platform/src/test/java/com/flywhl/saa/smartcs/support/ScsIntegrationTest.java
    - projects/smart-cs-platform/src/test/java/com/flywhl/saa/smartcs/AuthIntegrationTest.java
    - projects/smart-cs-platform/src/test/java/com/flywhl/saa/smartcs/ChatIntegrationTest.java
    - projects/smart-cs-platform/src/test/java/com/flywhl/saa/smartcs/TicketIntegrationTest.java
    - projects/smart-cs-platform/src/test/java/com/flywhl/saa/smartcs/ModelIntegrationTest.java
    - projects/smart-cs-platform/src/test/java/com/flywhl/saa/smartcs/SmartCsApplicationTests.java
    - projects/smart-cs-platform/src/test/java/com/flywhl/saa/smartcs/service/AuthServiceTest.java
    - projects/smart-cs-platform/src/test/java/com/flywhl/saa/smartcs/service/HybridSearchServiceTest.java
    - projects/smart-cs-platform/scripts/uat-smart-cs.sh
    - .planning/phases/06-smart-cs-platform/06-UAT.md
  modified:
    - projects/smart-cs-platform/src/test/resources/application-test.yml
    - projects/smart-cs-platform/src/test/resources/application-it.yml

key-decisions:
  - "IT 基座统一 @MockBean milvus/ES/redisStack/csIntentRouter，无 API Key 可跑"
  - "Docker 不可用时 DockerAvailableCondition 跳过 IT，CI 无 Key 仍绿"
  - "HANDOFF TODO 扫描使用词边界，避免 mapToDouble 假阳性"

patterns-established:
  - "ScsIntegrationTest + ScsPostgresRedisITBase 对齐 kqa/office IT 模式"
  - "uat-smart-cs.sh：无 Key 验 login/health/RBAC，有 Key 验 ask/stream/handoff/dashboard"

requirements-completed: [REQ-phase-6-smart-cs]

duration: 8min
completed: 2026-07-17
---

# Phase 06 Plan 07: 测试与 UAT 收口 Summary

**无 Key `mvn clean install` 全绿 + Testcontainers IT（Docker 门控）+ `uat-smart-cs.sh`/`06-UAT.md` 交付 HANDOFF §7 验收闭环。**

## Performance

- **Duration:** ~8 min
- **Started:** 2026-07-17T14:29:34Z
- **Completed:** 2026-07-17T14:37:00Z
- **Tasks:** 3/3
- **Files modified:** 20+

## Accomplishments

- Testcontainers 基座（PostgreSQL `scs_platform` + Redis 7）与核心单测（Auth / HybridSearch RRF / 既有 SemanticCache、Ticket）
- Auth / Chat / Ticket IT（无 Key）+ ModelIntegrationTest（API Key 门控）
- `uat-smart-cs.sh` + `06-UAT.md`；`clean install` / version-audit / spring-ai-2-readiness 通过

## Task Commits

1. **Task 1: Testcontainers 基座 + 单元测试** - `6f7b431` (feat)
2. **Task 2: Integration Tests + API Key 门控** - `4f22bfb` (feat)
3. **Task 3: HANDOFF §7 + uat-smart-cs.sh + 06-UAT.md** - `fac8934` (feat)

**Plan metadata:** （docs commit 见下）

## Files Created/Modified

- `ScsPostgresRedisITBase.java` / `DockerAvailableCondition.java` / `ScsIntegrationTest.java` — IT 基座
- `AuthServiceTest` / `HybridSearchServiceTest` / `SmartCsApplicationTests` — 单测与冒烟
- `AuthIntegrationTest` / `ChatIntegrationTest` / `TicketIntegrationTest` / `ModelIntegrationTest` — IT
- `application-test.yml` / `application-it.yml` / `src/test/resources/db/*` — 测试配置与 SQL
- `scripts/uat-smart-cs.sh` — 端口 19300 smoke
- `06-UAT.md` — 全接口 curl 清单

## Decisions Made

- IT 统一 Mock 向量库与 `csIntentRouter`，避免无 Milvus/ES/Key 时阻塞 CI
- `DockerAvailableCondition` 探测 OrbStack socket；Docker 守护进程未启动时 IT 跳过（本机验证时 OrbStack 未运行）
- HANDOFF 废弃 API 扫描用 `\bTODO\b` 词边界，规避 `mapToDouble` 中 `ToDo` 子串假阳性

## Deviations from Plan

### Auto-fixed Issues

**1. [Rule 3 - Blocking] HANDOFF rg 门禁假阳性**
- **Found during:** Task 3
- **Issue:** 计划原文 `rg -ci 'TODO|…'` 会命中 JDK `mapToDouble`（含 `ToDo` 子串），恒非 0
- **Fix:** 验收改用词边界 `\bTODO\b|\bFIXME\b|SupervisorAgent|PromptChatMemoryAdvisor`；源码无真实 TODO/废弃 API
- **Files modified:** 无代码改动（扫描策略）
- **Verification:** 词边界扫描无命中；`mvn clean install` 成功

**2. [Rule 2 - Correctness] IT 基座预置外部依赖 Mock**
- **Found during:** Task 2
- **Issue:** 三向量库 + Agent 启动依赖真实中间件
- **Fix:** 在 `ScsPostgresRedisITBase` 统一 `@MockBean` milvus/ES/redisStack/csIntentRouter/RestClient/MilvusServiceClient/JedisPooled/FaqEtlPipeline
- **Files modified:** `ScsPostgresRedisITBase.java`
- **Committed in:** `4f22bfb`

### Deferred Issues

- 本机 Docker/OrbStack 守护进程未运行，Auth/Chat/Ticket IT 被跳过；有 Docker 时应全量执行
- 有 Key + infra 的 `uat-smart-cs.sh` 真机验收留给 `/gsd-verify-work`

## Threat Flags

无新增威胁面（测试与 UAT 脚本；T-06-12 accept / T-06-SC mitigate 已遵守）。

## Known Stubs

None.

## Self-Check: PASSED

- [x] `ScsPostgresRedisITBase.java` 存在
- [x] `uat-smart-cs.sh` 可执行且含 `19300`
- [x] `06-UAT.md` 含 `api/auth/login`
- [x] commits `6f7b431` / `4f22bfb` / `fac8934` 存在
- [x] `mvn -f projects/smart-cs-platform/pom.xml clean install` 无 Key 成功
