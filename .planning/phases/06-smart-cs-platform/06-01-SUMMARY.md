---
phase: 06-smart-cs-platform
plan: 01
subsystem: database
tags: [spring-boot, jpa, postgresql, docker-compose, mapstruct, agent-framework, milvus, elasticsearch, redis-stack]

# Dependency graph
requires:
  - phase: 04-knowledge-qa-platform
    provides: pom/README/DDL/compose/Security/Entity 骨架模式（saa-learning-common/starter 复用范式）
  - phase: 05-office-agent-assistant
    provides: agent-framework 依赖装配与 Agent 编排项目结构参照
provides:
  - smart-cs-platform 独立 Maven 工程骨架（端口 19300，包根 com.flywhl.saa.smartcs），mvn compile 绿
  - db/schema.sql（11 张业务表 SSOT）+ db/data.sql 演示数据（2 客户/1 坐席/1 admin/12 FAQ/2 历史工单）
  - docker-compose.override.yml：smartcs profile（scs-db-init 建库 + scs-redis-stack 6380 语义缓存）
  - http/api.http 全接口契约（Auth/Chat/Ticket/Handoff/Admin/可观测）
  - ScsProperties（scs.rag/cache/memory/security/ticket 配置绑定）
  - 11 个 JPA Entity + Repository、7 个 DTO、9 个 VO、4 个 MapStruct Converter
affects: [06-smart-cs-platform-02-config, 06-smart-cs-platform-03-faq-rag, 06-smart-cs-platform-04-agent, 06-smart-cs-platform-05-conversation-ticket, 06-smart-cs-platform-06-admin, 06-smart-cs-platform-07-testing]

# Tech tracking
tech-stack:
  added:
    - spring-ai-alibaba-agent-framework（Agent 编排，Wave 3 消费）
    - spring-ai-starter-vector-store-milvus / -elasticsearch / -redis（FAQ 双库 + 语义缓存）
    - spring-ai-rag（RetrievalAugmentationAdvisor，Wave 2 消费）
    - spring-ai-alibaba-starter-nacos-prompt
  patterns:
    - "JPA ddl-auto=none，db/schema.sql 为唯一 DDL 真源"
    - "role/status 列用 @Enumerated(EnumType.STRING) 绑定 model 包枚举（UserRole/TicketStatus/FaqArticleStatus）"
    - "JSONB 列用 @JdbcTypeCode(SqlTypes.JSON)（model_profile.options_json / audit_log.detail）"
    - "双 Redis 物理隔离：6379 会话记忆 / 6380 Redis Stack 语义缓存（scs.cache.redis-uri 独立配置）"
    - "ScsProperties record + 紧凑构造器兜底默认值（同 Phase 4 KqaProperties 模式）"

key-files:
  created:
    - projects/smart-cs-platform/pom.xml
    - projects/smart-cs-platform/README.md
    - projects/smart-cs-platform/src/main/resources/application.yml
    - projects/smart-cs-platform/src/main/java/com/flywhl/saa/smartcs/SmartCsApplication.java
    - projects/smart-cs-platform/db/schema.sql
    - projects/smart-cs-platform/db/data.sql
    - projects/smart-cs-platform/docker-compose.override.yml
    - projects/smart-cs-platform/http/api.http
    - projects/smart-cs-platform/src/main/java/com/flywhl/saa/smartcs/config/ScsProperties.java
    - projects/smart-cs-platform/src/main/java/com/flywhl/saa/smartcs/model/entity/*.java（11 个）
    - projects/smart-cs-platform/src/main/java/com/flywhl/saa/smartcs/repository/*.java（11 个）
    - projects/smart-cs-platform/src/main/java/com/flywhl/saa/smartcs/model/dto/*.java（7 个）
    - projects/smart-cs-platform/src/main/java/com/flywhl/saa/smartcs/model/vo/*.java（9 个）
    - projects/smart-cs-platform/src/main/java/com/flywhl/saa/smartcs/mapper/*.java（4 个）
  modified: []

key-decisions:
  - "Rule 2 自动补依赖：pom.xml 追加 spring-ai-starter-vector-store-redis（Wave 2 SemanticCacheService 依赖此 VectorStore Bean，acceptance criteria 未列出但 06-RESEARCH/06-PATTERNS 明确要求语义缓存走 Redis Stack VectorStore，遗漏会阻塞后续波次编译）"
  - "role/status 列采用 @Enumerated(EnumType.STRING) 而非裸 String（较 Phase 4 SysUser.role 的裸 String 模式改进，复用新建的 UserRole/TicketStatus/FaqArticleStatus 枚举，服务层可直接做类型安全的状态机判断）"
  - "TicketStatus 枚举 Javadoc 内嵌合法转移图，与 06-RESEARCH Pattern 3 状态机锁定一致，供 Wave 5 TicketService 实现时直接引用"
  - "DTO/VO 集合在计划列出的必需项外，补充 UserVO/LoginVO/ChatMessageVO（计划用词「等」允许），因 http/api.http 的 Auth/会话历史契约需要对应载体，避免 Wave 4 再补基础模型"

patterns-established:
  - "Pattern: 双 Redis 隔离——scs.cache.redis-uri 与 spring.data.redis 分离配置，避免 RediSearch 与普通 Redis 混用（CLAUDE.md 已知注意点）"
  - "Pattern: model 包放跨 Entity/Service 复用的枚举（UserRole/TicketStatus/FaqArticleStatus），entity/dto/vo/mapper 各自子包按类型收敛"

requirements-completed: [REQ-phase-6-smart-cs]

# Metrics
duration: 45min
completed: 2026-07-10
---

# Phase 6 Plan 1: smart-cs-platform 绿field 工程骨架 Summary

**smart-cs-platform 独立 Maven 工程骨架：pom/README/DDL(11表)/演示数据/compose 双 Redis 编排/api.http 全契约 + ScsProperties/11 Entity/11 Repository/7 DTO/9 VO/4 Mapper，mvn compile 全绿零 TODO**

## Performance

- **Duration:** 约 45 分钟
- **Started:** 2026-07-10（本次执行会话）
- **Completed:** 2026-07-10T11:35:22Z
- **Tasks:** 3/3
- **Files modified:** 54（全部新建）

## Accomplishments

- 新建 `projects/smart-cs-platform` 绿field Maven 工程，parent 指向仓库父 POM，未挂父 POM `<modules>`，端口 19300，包根 `com.flywhl.saa.smartcs`
- `db/schema.sql` 一次性设计齐全 11 张业务表（用户/FAQ元数据/FAQChunk/会话/消息/工单/工单事件/Prompt/模型配置/审计/反馈），DDL 为 SSOT（JPA `ddl-auto=none`）
- `docker-compose.override.yml` 的 `smartcs` profile 提供 `scs-db-init`（建库+导入 DDL/演示数据）与 `scs-redis-stack`（6380，语义缓存专用，与会话记忆 6379 物理隔离）
- 全量数据契约就绪：11 个 JPA Entity 列名与 DDL 完全对齐、11 个 Repository 覆盖关键索引查询、7 个 DTO（jakarta.validation）、9 个 VO、4 个 MapStruct Converter
- `mvn -f projects/smart-cs-platform/pom.xml compile` 全绿，Entity 数量(11)与 DDL 表数量(11)一致，源码零 TODO/FIXME/骨架占位

## Task Commits

Each task was committed atomically:

1. **Task 1: pom + Application + README + application.yml** - `01fd98b` (feat)
2. **Task 2: DDL + 演示数据 + compose override + api.http** - `f034a69` (feat)
3. **Task 3: ScsProperties + Entity + Repository + DTO/VO + Mapper** - `b5a764b` (feat)

**Plan metadata:** (this commit, docs: complete plan)

## Files Created/Modified

- `projects/smart-cs-platform/pom.xml` - 独立工程依赖集（common/starter + web/security/jpa/redis + dashscope/deepseek + agent-framework + rag + milvus/es/redis 向量库 + nacos-prompt + knife4j）
- `projects/smart-cs-platform/README.md` - 业务场景 Mermaid、技术落点、目录骨架、演示账号、infra 启动命令
- `projects/smart-cs-platform/src/main/resources/application.yml` - 端口 19300、scs_platform 数据源、双 Redis、Milvus/ES/Redis Stack 向量库配置、scs.* 骨架
- `projects/smart-cs-platform/src/main/java/com/flywhl/saa/smartcs/SmartCsApplication.java` - 启动入口，装配 ScsProperties/JPA Repositories/事务
- `projects/smart-cs-platform/db/schema.sql` - 11 张表 DDL SSOT
- `projects/smart-cs-platform/db/data.sql` - 演示账号/FAQ种子/历史会话工单
- `projects/smart-cs-platform/docker-compose.override.yml` - smartcs profile 服务编排
- `projects/smart-cs-platform/http/api.http` - 全接口 REST Client 契约
- `projects/smart-cs-platform/src/main/java/com/flywhl/saa/smartcs/config/ScsProperties.java` - scs.* 配置绑定
- `projects/smart-cs-platform/src/main/java/com/flywhl/saa/smartcs/model/{UserRole,TicketStatus,FaqArticleStatus}.java` - 三枚举
- `projects/smart-cs-platform/src/main/java/com/flywhl/saa/smartcs/model/entity/*.java` - 11 个 Entity
- `projects/smart-cs-platform/src/main/java/com/flywhl/saa/smartcs/repository/*.java` - 11 个 Repository
- `projects/smart-cs-platform/src/main/java/com/flywhl/saa/smartcs/model/dto/*.java` - 7 个 DTO
- `projects/smart-cs-platform/src/main/java/com/flywhl/saa/smartcs/model/vo/*.java` - 9 个 VO
- `projects/smart-cs-platform/src/main/java/com/flywhl/saa/smartcs/mapper/*.java` - 4 个 MapStruct Converter

## Decisions Made

- Entity 的 role/status 列改用 `@Enumerated(EnumType.STRING)` 绑定新建枚举（而非 Phase 4 kqa 的裸 String 模式），为 Wave 5 `TicketService` 状态机提供类型安全基础
- FAQ 语义缓存的 Redis Stack VectorStore 依赖（`spring-ai-starter-vector-store-redis`）在本 Wave 提前加入 pom，避免 Wave 2 `SemanticCacheService` 落地时才发现缺依赖（详见下方 Deviations）
- DTO/VO 集合按计划列出的 7 DTO + 6 VO 核心项为主，另补 `UserVO`/`LoginVO`/`ChatMessageVO`（计划用词「等」覆盖），支撑 `api.http` 中 Auth 登录响应与会话历史查询的最小契约需求

## Deviations from Plan

### Auto-fixed Issues

**1. [Rule 2 - Missing Critical] pom.xml 补充 `spring-ai-starter-vector-store-redis` 依赖**
- **Found during:** Task 1（pom 依赖装配）
- **Issue:** 计划 acceptance_criteria 的依赖清单未显式列出 Redis VectorStore；但 06-RESEARCH.md「FAQ 语义缓存」与 06-PATTERNS.md `SemanticCacheService` 均明确要求 Redis Stack（6380）作为独立 VectorStore Bean 承载语义缓存，缺失该依赖会导致 Wave 2 波次编译失败
- **Fix:** 在 pom.xml 追加 `org.springframework.ai:spring-ai-starter-vector-store-redis`（零版本号，父 BOM 管理），并在 `application.yml` 补充 `spring.ai.vectorstore.redis.*` 与 `scs.cache.redis-uri` 配置骨架，指向 6380 端口
- **Files modified:** `projects/smart-cs-platform/pom.xml`, `projects/smart-cs-platform/src/main/resources/application.yml`
- **Verification:** `mvn -f projects/smart-cs-platform/pom.xml compile` 通过（依赖可解析）
- **Committed in:** `01fd98b`（Task 1 commit）

---

**Total deviations:** 1 auto-fixed（1 missing critical）
**Impact on plan:** 补充依赖是后续波次（Wave 2 语义缓存）能够编译成立的前提条件，未扩大本 Wave 的业务逻辑范围，仅完善数据契约层的依赖闭环。

## Issues Encountered

- Task 3 的官方 verify 命令 `rg ... | rg -ci 'TODO|FIXME|骨架占位' | grep -q '^0$'` 在零匹配场景下无法通过：ripgrep 在管道输入无匹配时不打印 "0" 而是静默退出码 1，导致 `grep -q '^0$'` 永远失败。已通过等价语义验证（`rg ... | rg -ci ...` 退出码 1 = 零匹配 = 干净）确认源码无 TODO/FIXME/骨架占位，此为计划验证脚本本身的已知缺陷，不影响交付质量，建议后续 plan 编写时改用 `grep -c` 或显式统计后比较。

## User Setup Required

None - 无需外部服务人工配置。Wave 0 仅产出可编译的骨架与配置契约；真实中间件（PostgreSQL/Milvus/ES/Redis Stack/Nacos）与 `AI_DASHSCOPE_API_KEY` 由 Wave 1~6 及 UAT 阶段按需拉起。

## Next Phase Readiness

- config 波次（Wave 1）可直接注入本 Wave 产出的 `ScsProperties`、11 个 `Repository`，实现 Security/Milvus/ES/Redis 记忆/Redis Stack 缓存/Nacos/`AiClientConfig` 装配
- `db/schema.sql` + `docker-compose.override.yml` 已就绪，`scripts/infra.sh` 或直接 `docker compose --profile smartcs up -d` 可拉起 `scs_platform` 库与语义缓存 Redis Stack
- `http/api.http` 契约已覆盖全部域（Auth/Chat/Ticket/Handoff/Admin/可观测），后续波次的 Controller 实现可直接对照验收
- 无阻塞项；Wave 2 需注意消费本 Wave 新增的 `spring-ai-starter-vector-store-redis` 依赖装配 `redisStackVectorStore` Bean（与会话记忆 `RedisChatMemoryRepository` 使用的 `spring.data.redis` 连接需保持物理隔离）

---
*Phase: 06-smart-cs-platform*
*Completed: 2026-07-10*
