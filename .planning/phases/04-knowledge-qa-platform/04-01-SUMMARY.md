---
phase: 04-knowledge-qa-platform
plan: 01
subsystem: database
tags: [jpa, mapstruct, spring-boot, postgresql, configuration-properties]

requires:
  - phase: 03-48-demos
    provides: Spring AI / RAG / Milvus / Redis 可复用 Demo 模式
provides:
  - 标准 Maven 包路径 com/flywhl/saa/knowledgeqa/**
  - KqaProperties 绑定 kqa.minio/rag/memory/security.jwt
  - 8 张表 JPA Entity + 8 Repository
  - 4 MapStruct Converter + DTO/VO record 契约
affects: [04-02-config, 04-03-rag, 04-04-qa, 04-05-admin]

tech-stack:
  added: [MapStruct, Spring Data JPA, Hibernate JSONB]
  patterns: ["@ConfigurationProperties record 紧凑构造器默认值", "Entity 列名对齐 schema.sql SSOT", "JpaRepository 派生查询按索引列"]

key-files:
  created:
    - projects/knowledge-qa-platform/src/main/java/com/flywhl/saa/knowledgeqa/model/dto/UserCreateRequest.java
    - projects/knowledge-qa-platform/src/main/java/com/flywhl/saa/knowledgeqa/model/vo/UserVO.java
    - projects/knowledge-qa-platform/src/main/java/com/flywhl/saa/knowledgeqa/model/vo/PromptTemplateVO.java
  modified:
    - projects/knowledge-qa-platform/src/main/java/com/flywhl/saa/knowledgeqa/config/KqaProperties.java
    - projects/knowledge-qa-platform/src/main/java/com/flywhl/saa/knowledgeqa/model/entity/*.java
    - projects/knowledge-qa-platform/src/main/java/com/flywhl/saa/knowledgeqa/repository/*.java
    - projects/knowledge-qa-platform/src/main/java/com/flywhl/saa/knowledgeqa/mapper/*.java
    - projects/knowledge-qa-platform/src/main/java/com/flywhl/saa/knowledgeqa/model/dto/*.java
    - projects/knowledge-qa-platform/src/main/java/com/flywhl/saa/knowledgeqa/model/vo/*.java

key-decisions:
  - "JSONB 字段（qa_message.citations、audit_log.detail）使用 @JdbcTypeCode(SqlTypes.JSON)"
  - "PromptTemplateVO 额外添加以支撑 PromptConverter entity↔VO 映射"
  - "保留骨架 DocumentUploadRequest 供后续 multipart 上传校验"

patterns-established:
  - "KqaProperties 仿 SaaLearningProperties：record + 紧凑构造器填 application.yml 默认值"
  - "Entity 外键 @ManyToOne(fetch=LAZY)，TIMESTAMPTZ → OffsetDateTime"
  - "DTO 一律 Java record + jakarta.validation；VO 一律不可变 record"

requirements-completed: [REQ-phase-4-knowledge-qa]

duration: 12min
completed: 2026-07-05
---

# Phase 4 Plan 01: Wave 0 + 数据层地基 Summary

**畸形包路径修复 + KqaProperties + 8 JPA Entity + 8 Repository + 4 MapStruct + DTO/VO 契约，`mvn compile` 通过**

## Performance

- **Duration:** 12 min
- **Started:** 2026-07-05T14:50:00Z
- **Completed:** 2026-07-05T15:02:00Z
- **Tasks:** 3
- **Files modified:** 97

## Accomplishments

- 63 个占位类从 `com\/flywhl\/` 及子包反斜杠目录迁至标准 `com/flywhl/saa/knowledgeqa/**`
- `KqaProperties` record 绑定 minio/rag/memory/security.jwt 四组配置，默认值对齐 application.yml
- 8 个 `@Entity` 列名与 `db/schema.sql` 完全一致，含 JSONB 与 lazy 外键
- 8 个 `JpaRepository`、4 个 MapStruct Converter、5 DTO + 7 VO record 编译绿

## Task Commits

Each task was committed atomically:

1. **Task 1: Wave 0 — 畸形包路径迁移** - `ace558a` (feat)
2. **Task 2: KqaProperties + 8 Entity** - `64886f2` (feat)
3. **Task 3: Repository + Mapper + DTO/VO** - `5e9019e` (feat)

**Plan metadata:** `b6c856a` (docs: complete plan)

## Files Created/Modified

- `config/KqaProperties.java` — kqa.* 配置绑定 record
- `model/entity/*.java` — 8 张表 JPA 实体
- `repository/*.java` — Spring Data JPA 接口含派生查询
- `mapper/*.java` — MapStruct entity↔VO 转换
- `model/dto/*.java` — Login/Qa/Feedback/Prompt/UserCreate 请求 record
- `model/vo/*.java` — QaAnswer/Citation/Conversation/Document/Dashboard/User/PromptTemplate 视图 record

## Decisions Made

- JSONB 用 Hibernate 6 `@JdbcTypeCode(SqlTypes.JSON)` 映射 `List<Map>` / `Map`
- 新增 `PromptTemplateVO` 满足 PromptConverter 编译需求（计划列 6 VO 外扩展）
- `UserConverter.toEntity` 忽略 passwordHash，BCrypt 编码留给 AuthService（04-04）

## Deviations from Plan

### Auto-fixed Issues

**1. [Rule 3 - Blocking] 修复 admin/model 子包内反斜杠目录**
- **Found during:** Task 1
- **Issue:** 顶层 `com\/flywhl\/` 迁移后，`admin\/controller`、`model\/dto` 等子目录仍含字面反斜杠
- **Fix:** 二次迁移至 `admin/controller`、`model/dto` 等标准路径并删除空目录
- **Files modified:** admin/*, model/* 下 23 个文件
- **Committed in:** ace558a

**2. [Rule 2 - Missing Critical] 新增 PromptTemplateVO**
- **Found during:** Task 3
- **Issue:** PromptConverter 需 entity↔VO 目标类型，计划 6 VO 列表未含 Prompt 视图
- **Fix:** 添加 `PromptTemplateVO` record 供 MapStruct 映射
- **Files modified:** `model/vo/PromptTemplateVO.java`, `mapper/PromptConverter.java`
- **Committed in:** 5e9019e

---

**Total deviations:** 2 auto-fixed (1 blocking, 1 missing critical)
**Impact on plan:** 必要修正，无范围蔓延

## Issues Encountered

- 计划验证 `find ... | wc -l | grep '^65$'` 与实际 64 个 main 源文件（+1 test）不符；以 git 追踪文件为准，功能不受影响
- config/controller/service/rag 等 31 个类仍保留「骨架占位」注释，属 04-02~04-05 波次范围，本 plan 数据层文件已无占位字符串

## User Setup Required

None - no external service configuration required.

## Next Phase Readiness

- 04-02 可注入 `KqaProperties`、全部 Repository、DTO/VO 契约
- Entity 列名与 schema.sql 对齐，config 波次可直接装配 Security/MinIO/VectorStore
- 阻塞项 D-01（畸形路径）已解除

## Self-Check: PASSED

- FOUND: projects/knowledge-qa-platform/src/main/java/com/flywhl/saa/knowledgeqa/config/KqaProperties.java
- FOUND: projects/knowledge-qa-platform/src/main/java/com/flywhl/saa/knowledgeqa/model/entity/SysUser.java
- FOUND: ace558a, 64886f2, 5e9019e
- `mvn -f projects/knowledge-qa-platform/pom.xml compile` — PASSED

---
*Phase: 04-knowledge-qa-platform*
*Completed: 2026-07-05*
