---
phase: 04-knowledge-qa-platform
plan: 02
subsystem: infra
tags: [spring-security, jwt, minio, milvus, redis, knife4j, prompt-nacos]

requires:
  - phase: 04-01
    provides: KqaProperties、8 Repository、DTO/VO 契约
provides:
  - SecurityConfig JWT 资源服务器 + UserDetailsService
  - AsyncConfig etlExecutor 有界线程池
  - MinioClient / VectorStore 默认检索参数 / Redis ChatMemory / Knife4j 分组
  - PromptTemplateProvider 三级回退
affects: [04-03-rag, 04-04-qa, 04-05-admin]

tech-stack:
  added: [NimbusJwtEncoder, MessageChatMemoryAdvisor, GroupedOpenApi]
  patterns: ["OAuth2 Resource Server 对称密钥 JWT", "Redis List + TTL 会话记忆", "Prompt Nacos→DB→classpath 回退"]

key-files:
  created:
    - projects/knowledge-qa-platform/src/main/java/com/flywhl/saa/knowledgeqa/config/RedisChatMemoryRepository.java
    - projects/knowledge-qa-platform/src/main/java/com/flywhl/saa/knowledgeqa/config/MessageDto.java
  modified:
    - projects/knowledge-qa-platform/src/main/java/com/flywhl/saa/knowledgeqa/config/SecurityConfig.java
    - projects/knowledge-qa-platform/src/main/java/com/flywhl/saa/knowledgeqa/config/AsyncConfig.java
    - projects/knowledge-qa-platform/src/main/java/com/flywhl/saa/knowledgeqa/config/MinioConfig.java
    - projects/knowledge-qa-platform/src/main/java/com/flywhl/saa/knowledgeqa/config/VectorStoreConfig.java
    - projects/knowledge-qa-platform/src/main/java/com/flywhl/saa/knowledgeqa/config/ChatMemoryConfig.java
    - projects/knowledge-qa-platform/src/main/java/com/flywhl/saa/knowledgeqa/config/OpenApiConfig.java
    - projects/knowledge-qa-platform/src/main/java/com/flywhl/saa/knowledgeqa/prompt/PromptTemplateProvider.java
    - projects/knowledge-qa-platform/src/main/java/com/flywhl/saa/knowledgeqa/config/KqaProperties.java
    - projects/knowledge-qa-platform/src/main/resources/application.yml

key-decisions:
  - "JWT secret 经 KQA_JWT_SECRET 环境变量注入，application.yml 仅 dev 默认值"
  - "MessageChatMemoryAdvisor 单独暴露 Bean，ChatClient 装配留 04-03 避免循环依赖"
  - "OpenAPI 额外放行 /doc.html 与 swagger 路径便于 Knife4j 本地调试"

patterns-established:
  - "Redis ChatMemory key 前缀 kqa:chat-memory:，TTL 绑定 kqa.memory.ttl"
  - "VectorStoreConfig 暴露 RagRetrievalDefaults + defaultSearchRequest 供 RagPipelineFactory"

requirements-completed: [REQ-phase-4-knowledge-qa]

duration: 18min
completed: 2026-07-05
---

# Phase 4 Plan 02: 基础设施 Config 波次 Summary

**OAuth2 JWT 鉴权链 + MinIO/Milvus/Redis Memory Bean 图 + Knife4j 分组 + Prompt 三级回退，`mvn compile` 通过**

## Performance

- **Duration:** 18 min
- **Started:** 2026-07-05T14:58:00Z
- **Completed:** 2026-07-05T15:16:00Z
- **Tasks:** 3
- **Files modified:** 11

## Accomplishments

- SecurityConfig：Nimbus JWT 编解码、DelegatingPasswordEncoder、SysUser UserDetailsService、/api/** 鉴权
- AsyncConfig：etlExecutor（core=2 max=4 queue=100）供文档 ETL 异步流水线
- MinioConfig / VectorStoreConfig / ChatMemoryConfig / OpenApiConfig 四类中间件与文档配置就绪
- PromptTemplateProvider：Nacos → DB PUBLISHED → classpath `prompts/*.st` 静默降级

## Task Commits

Each task was committed atomically:

1. **Task 1: SecurityConfig + JWT + AsyncConfig** - `0d58ab9` (feat)
2. **Task 2: Minio + VectorStore + ChatMemory + OpenAPI** - `f2edf49` (feat)
3. **Task 3: PromptTemplateProvider** - `a9af2a7` (feat)

**Plan metadata:** `bfe8c7a` (docs: complete plan)

## Files Created/Modified

- `config/SecurityConfig.java` — JWT Resource Server + 匿名 login/health/doc
- `config/AsyncConfig.java` — `@EnableAsync` + `etlExecutor`
- `config/MinioConfig.java` — MinioClient Bean
- `config/VectorStoreConfig.java` — RagRetrievalDefaults、defaultSearchRequest、启动日志
- `config/ChatMemoryConfig.java` — MessageWindowChatMemory + MessageChatMemoryAdvisor
- `config/RedisChatMemoryRepository.java` — Redis List 持久化 + TTL
- `config/OpenApiConfig.java` — Knife4j auth/qa/admin 三组
- `prompt/PromptTemplateProvider.java` — 三级回退 get/getQueryRewriteTemplate

## Decisions Made

- KqaProperties.Jwt 增加 `secret` 字段，与 `KQA_JWT_SECRET` 环境变量对齐（T-04-03）
- 不在 ChatMemoryConfig 内创建带 RAG 的 ChatClient，避免与 04-03 RagPipelineFactory 循环依赖
- Knife4j/swagger 路径 permitAll，便于本机 API 文档调试

## Deviations from Plan

### Auto-fixed Issues

**1. [Rule 2 - Missing Critical] KqaProperties 补充 JWT secret 绑定**
- **Found during:** Task 1
- **Issue:** 计划要求 JWT 密钥从 kqa.security.jwt 读取，04-01 仅含 issuer/ttl
- **Fix:** Jwt record 增加 secret + application.yml `${KQA_JWT_SECRET}` 占位
- **Files modified:** KqaProperties.java, application.yml
- **Committed in:** 0d58ab9

**2. [Rule 1 - Bug] ObjectProvider.ifAvailable 双参数用法编译失败**
- **Found during:** Task 2
- **Issue:** Spring ObjectProvider 无 ifPresentOrElse 重载
- **Fix:** 改用 getIfAvailable() 空值判断
- **Files modified:** VectorStoreConfig.java
- **Committed in:** f2edf49

---

**Total deviations:** 2 auto-fixed (1 missing critical, 1 bug)
**Impact on plan:** 必要修正，无范围蔓延

## Issues Encountered

None

## User Setup Required

运行时需配置（compile 不依赖）：
- `AI_DASHSCOPE_API_KEY` — 04-03 AiClientConfig
- `DEEPSEEK_API_KEY` — FallbackModelRouter 备用通道
- `KQA_JWT_SECRET` — 生产环境 JWT 签名（dev 有默认值）

## Next Phase Readiness

- 04-03 可注入 MessageChatMemoryAdvisor、RagRetrievalDefaults、PromptTemplateProvider、JwtEncoder
- AiClientConfig / RagPipelineFactory 可装配 Modular RAG 链
- 应用完整启动仍依赖 04-03/04-04 Controller/Service 实现

## Self-Check: PASSED

- FOUND: projects/knowledge-qa-platform/src/main/java/com/flywhl/saa/knowledgeqa/config/SecurityConfig.java
- FOUND: projects/knowledge-qa-platform/src/main/java/com/flywhl/saa/knowledgeqa/prompt/PromptTemplateProvider.java
- FOUND: 0d58ab9, f2edf49, a9af2a7
- `mvn -f projects/knowledge-qa-platform/pom.xml compile` — PASSED
- `rg PromptChatMemoryAdvisor|FunctionCallback` — 无匹配

---
*Phase: 04-knowledge-qa-platform*
*Completed: 2026-07-05*
