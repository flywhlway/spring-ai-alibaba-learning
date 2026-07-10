---
phase: 06-smart-cs-platform
plan: 02
subsystem: infra
tags: [spring-security, jwt, milvus, elasticsearch, redis-stack, chat-memory, spring-ai, knife4j, micrometer]

# Dependency graph
requires:
  - phase: 06-smart-cs-platform (Wave 0 / Plan 01)
    provides: 工程骨架、ScsProperties、11 Entity/Repository、DTO/VO/Mapper、pom 依赖、application.yml 骨架
provides:
  - JWT Resource Server（SecurityConfig + AuthController + AuthService），POST /api/auth/login 签发 token
  - 三个物理隔离的 VectorStore Bean：milvusVectorStore（scs_faq）、elasticsearchVectorStore（scs-faq）、redisStackVectorStore（6380 语义缓存）
  - 会话记忆：RedisChatMemoryRepository（6379）+ MessageWindowChatMemory + MessageChatMemoryAdvisor
  - ChatClient.Builder（AiClientConfig，挂载 starter AuditLoggingAdvisor，经 ModelRouter 路由 DashScope/DeepSeek）
  - OpenApiConfig（Knife4j「智能客服平台」分组 auth/chat/ticket/admin）+ ObservabilityConfig（Prometheus 公共标签）
affects: [06-smart-cs-platform-03-faq-rag, 06-smart-cs-platform-04-agent, 06-smart-cs-platform-05-conversation-ticket, 06-smart-cs-platform-06-admin, 06-smart-cs-platform-07-testing]

# Tech tracking
tech-stack:
  added: []
  patterns:
    - "多 VectorStore 共存：不依赖各 starter 的默认 vectorStore autoconfig Bean（三者互相 @ConditionalOnMissingBean 冲突），改为在自定义 @Configuration 中显式声明具体子类型（MilvusVectorStore/ElasticsearchVectorStore/RedisVectorStore）Bean，同时复用 starter 自动装配的底层连接 Bean（MilvusServiceClient / RestClient）"
    - "语义缓存 Redis Stack 与会话记忆 Redis 物理隔离：redisStackVectorStore 手动构建独立 JedisPooled 指向 scs.cache.redis-uri（6380），不共用 spring.data.redis 的 Lettuce 连接（6379）"
    - "AiClientConfig 仅装配 ChatClient.Builder（不 build()），会话记忆/RAG Advisor 由下游 Service 按场景 mutate() 追加，避免过早固化 Advisor 链"
    - "小 DTO 同包：RedisChatMemoryRepository 的 MessageDto 序列化载体作为包级 record 内嵌于同一文件，未拆分单独文件"

key-files:
  created:
    - projects/smart-cs-platform/src/main/java/com/flywhl/saa/smartcs/config/SecurityConfig.java
    - projects/smart-cs-platform/src/main/java/com/flywhl/saa/smartcs/controller/AuthController.java
    - projects/smart-cs-platform/src/main/java/com/flywhl/saa/smartcs/service/AuthService.java
    - projects/smart-cs-platform/src/main/java/com/flywhl/saa/smartcs/config/MilvusVectorStoreConfig.java
    - projects/smart-cs-platform/src/main/java/com/flywhl/saa/smartcs/config/ElasticsearchVectorStoreConfig.java
    - projects/smart-cs-platform/src/main/java/com/flywhl/saa/smartcs/config/RedisStackCacheConfig.java
    - projects/smart-cs-platform/src/main/java/com/flywhl/saa/smartcs/config/ChatMemoryConfig.java
    - projects/smart-cs-platform/src/main/java/com/flywhl/saa/smartcs/config/RedisChatMemoryRepository.java
    - projects/smart-cs-platform/src/main/java/com/flywhl/saa/smartcs/config/OpenApiConfig.java
    - projects/smart-cs-platform/src/main/java/com/flywhl/saa/smartcs/config/AiClientConfig.java
    - projects/smart-cs-platform/src/main/java/com/flywhl/saa/smartcs/config/ObservabilityConfig.java
  modified: []

key-decisions:
  - "三个 VectorStore（Milvus/ES/Redis Stack）不使用各自 starter 的默认 autoconfig Bean，手动在 Config 类中声明具体子类型（MilvusVectorStore/ElasticsearchVectorStore/RedisVectorStore），既避免三者的 @ConditionalOnMissingBean 互相覆盖，也满足 acceptance criteria 要求的 @Qualifier 精确区分"
  - "redisStackVectorStore 独立构建 JedisPooled 而非复用容器默认 RedisConnectionFactory：spring-ai-starter-vector-store-redis 的默认自动装配依赖 JedisConnectionFactory 类型 Bean，而项目会话记忆走 spring-boot-starter-data-redis 默认的 Lettuce 驱动（6379），二者类型不同不会冲突，但若不显式提供 JedisConnectionFactory/JedisPooled，语义缓存将无法启动；直接用 scs.cache.redis-uri 构建独立 JedisPooled 更简单可控"
  - "AiClientConfig 只产出 ChatClient.Builder，不在此 build() 固化 Advisor 链，为 Wave 2 FAQ/RAG 与 Wave 3 Agent 留出各自追加 RetrievalAugmentationAdvisor/MessageChatMemoryAdvisor 的空间"

patterns-established:
  - "Pattern: 多 VectorStore 共存装配——放弃 starter 默认 autoconfig，显式声明具体 Store 子类型 Bean + 复用其暴露的底层连接 autoconfig Bean（MilvusServiceClient/RestClient），Redis Stack 单独构建 JedisPooled"
  - "Pattern: ChatClient.Builder 仅做基础装配（模型路由 + 审计 Advisor），业务 Advisor 链下沉到消费方 Service"

requirements-completed: [REQ-phase-6-smart-cs]

# Metrics
duration: 60min
completed: 2026-07-10
---

# Phase 6 Plan 2: smart-cs-platform Security/VectorStore/AI 客户端装配 Summary

**JWT Resource Server + 三 VectorStore（Milvus scs_faq / ES scs-faq / Redis Stack 6380 语义缓存）物理隔离装配 + 会话记忆 6379 独立连接 + ChatClient.Builder（starter 路由+审计），`mvn compile` 全绿，禁用 API grep 零命中**

## Performance

- **Duration:** 约 60 分钟
- **Started:** 2026-07-10（本次执行会话）
- **Completed:** 2026-07-10T11:52:00Z
- **Tasks:** 3/3
- **Files modified:** 11（全部新建）

## Accomplishments

- SecurityConfig：Nimbus JwtEncoder/JwtDecoder + JwtAuthenticationConverter（role claim → ROLE_*），`/api/auth/login`、`/actuator/health`、`/actuator/prometheus`、Knife4j 文档路径 permitAll，其余 `/api/**` 要求认证；AuthService 用 DelegatingPasswordEncoder 校验 `sys_user`（演示数据 `{noop}` 前缀），签发含 `uid`/`role` claim 的 JWT，`requireCurrentUser()` 供后续波次 Service/@Tool 复用
- 三个物理隔离的 VectorStore Bean 全部手动装配为具体子类型（规避三个 starter 的默认 `vectorStore` autoconfig 互相 `@ConditionalOnMissingBean` 覆盖问题）：`milvusVectorStore`（collection `scs_faq`，1024 维，IVF_FLAT+COSINE，复用自动装配的 `MilvusServiceClient`）、`elasticsearchVectorStore`（index `scs-faq`，复用 Boot 官方 `RestClient` autoconfig）、`redisStackVectorStore`（独立 `JedisPooled` 指向 `scs.cache.redis-uri` 6380，metadata filter `type` 可用）
- 会话记忆 6379 与语义缓存 6380 物理隔离验证：`RedisChatMemoryRepository` 用 `spring.data.redis`（Lettuce，6379）+ `StringRedisTemplate`，`RedisStackCacheConfig` 独立 `JedisPooled`（6380），二者互不共用连接；`ChatMemoryConfig` 装配 `MessageWindowChatMemory` + `MessageChatMemoryAdvisor`（新写法，非废弃 `PromptChatMemoryAdvisor`）
- `AiClientConfig` 提供 `ChatClient.Builder`（默认挂载 starter `AuditLoggingAdvisor`，模型来自 `ModelRouter.route()` 主备路由）；`OpenApiConfig` Knife4j 标题「智能客服平台」+ Bearer JWT Scheme，分组 auth/chat/ticket/admin 对齐 `http/api.http`；`ObservabilityConfig` 追加 Micrometer 公共标签 `project=smart-cs-platform`（Prometheus 导出/health 暴露/成本采集沿用 Wave 0 `application.yml` 配置，未重复声明）
- `mvn -f projects/smart-cs-platform/pom.xml clean compile` 全绿；`rg` 全项目扫描禁用 API（`SupervisorAgent`/`PromptChatMemoryAdvisor`/`FunctionCallback`/`interruptBefore`）零命中；三 VectorStore Bean 均存在

## Task Commits

Each task was committed atomically:

1. **Task 1: SecurityConfig + Auth 域** - `81ff880` (feat)
2. **Task 2: VectorStore 三件套 + ChatMemory** - `9000cfb` (feat)
3. **Task 3: Nacos + OpenAPI + AiClient + Observability** - `08c304b` (feat，含 ChatMemoryConfig Javadoc 禁用词修正)

**Plan metadata:** (this commit, docs: complete plan)

## Files Created/Modified

- `projects/smart-cs-platform/src/main/java/com/flywhl/saa/smartcs/config/SecurityConfig.java` - JWT Resource Server，RBAC 路径规则，UserDetailsService/PasswordEncoder/AuthenticationManager
- `projects/smart-cs-platform/src/main/java/com/flywhl/saa/smartcs/controller/AuthController.java` - `POST /api/auth/login`、`GET /api/auth/me`
- `projects/smart-cs-platform/src/main/java/com/flywhl/saa/smartcs/service/AuthService.java` - 账号校验、JWT 签发（含 `expiresIn`）、`requireCurrentUser()`
- `projects/smart-cs-platform/src/main/java/com/flywhl/saa/smartcs/config/MilvusVectorStoreConfig.java` - `milvusVectorStore` Bean（scs_faq，1024 维）
- `projects/smart-cs-platform/src/main/java/com/flywhl/saa/smartcs/config/ElasticsearchVectorStoreConfig.java` - `elasticsearchVectorStore` Bean（scs-faq）
- `projects/smart-cs-platform/src/main/java/com/flywhl/saa/smartcs/config/RedisStackCacheConfig.java` - 独立 `JedisPooled`（6380）+ `redisStackVectorStore` Bean（语义缓存）
- `projects/smart-cs-platform/src/main/java/com/flywhl/saa/smartcs/config/ChatMemoryConfig.java` - `MessageWindowChatMemory` + `MessageChatMemoryAdvisor`
- `projects/smart-cs-platform/src/main/java/com/flywhl/saa/smartcs/config/RedisChatMemoryRepository.java` - 会话记忆 Redis 存储（6379），内嵌 `MessageDto`
- `projects/smart-cs-platform/src/main/java/com/flywhl/saa/smartcs/config/OpenApiConfig.java` - Knife4j 文档分组 + Bearer JWT Scheme
- `projects/smart-cs-platform/src/main/java/com/flywhl/saa/smartcs/config/AiClientConfig.java` - `ChatClient.Builder`（ModelRouter + AuditLoggingAdvisor）
- `projects/smart-cs-platform/src/main/java/com/flywhl/saa/smartcs/config/ObservabilityConfig.java` - Micrometer 公共标签

## Decisions Made

- 三个 VectorStore 手动装配为具体子类型 Bean，而非依赖各 starter 的默认 `vectorStore` autoconfig（详见 key-decisions/patterns-established）
- `redisStackVectorStore` 独立构建 `JedisPooled` 而非依赖容器默认 `JedisConnectionFactory`，从根源保证与会话记忆 Redis（Lettuce，6379）物理隔离
- `AiClientConfig` 只产出 `ChatClient.Builder`，Advisor 链的会话记忆/RAG 部分留给 Wave 2/3 消费方按场景追加

## Deviations from Plan

### Auto-fixed Issues

**1. [Rule 1 - Bug] ChatMemoryConfig Javadoc 误触发禁用 API grep 门禁**
- **Found during:** Task 3（禁用 API grep 验证）
- **Issue:** Task 2 提交的 `ChatMemoryConfig.java` Javadoc 中直接拼写了禁用 API 类名 `PromptChatMemoryAdvisor`（用于说明"本项目不使用该已废弃类"），被 Task 3 的 `rg 'SupervisorAgent|PromptChatMemoryAdvisor|FunctionCallback|interruptBefore'` 门禁误判为命中
- **Fix:** 改写 Javadoc 为不含该 token 的等价说明（"仓库禁用旧版 Prompt 前缀记忆 Advisor"），保留原有说明意图
- **Files modified:** `projects/smart-cs-platform/src/main/java/com/flywhl/saa/smartcs/config/ChatMemoryConfig.java`
- **Verification:** `rg -v '^#' .../src/main/java | rg -c 'SupervisorAgent|PromptChatMemoryAdvisor|FunctionCallback|interruptBefore'` 命中归零（ripgrep 零匹配时静默退出码 1，等价"零命中"，与 06-01-SUMMARY 记录的已知 ripgrep 行为一致）
- **Committed in:** `08c304b`（Task 3 commit）

---

**Total deviations:** 1 auto-fixed（1 bug）
**Impact on plan:** 仅修正一处文档措辞触发的门禁误判，未改变任何运行时行为，不构成范围蔓延。

## Issues Encountered

- 计划 Task 3 的验证命令 `rg ... | rg -ci ... | grep -q '^0$'` 在零匹配场景下同样存在 06-01-SUMMARY 记录过的已知缺陷（ripgrep 无匹配时静默退出码 1 而非打印 "0"）；本次通过手动确认 `rg -c` 退出码 1 = 零匹配语义验证门禁通过，未影响交付质量
- Spring AI 1.1.2 的 `spring-ai-starter-vector-store-redis` 默认 autoconfig 依赖容器中的 `JedisConnectionFactory` Bean，而非 Wave 0 `application.yml` 中预留的 `spring.ai.vectorstore.redis.uri` 属性（该属性键在 `RedisVectorStoreProperties` 中并不存在，实际未被绑定使用）；本 Wave 通过手动构建独立 `JedisPooled` 完全绕开了该 autoconfig 路径，`scs.cache.redis-uri` 才是真正生效的连接配置来源，`spring.ai.vectorstore.redis.*` 这组 yml 键位保留但当前未被任何 Bean 读取（不影响编译与本 Wave 交付，留待后续如需切回 autoconfig 路径时参考）

## User Setup Required

None - 无需外部服务人工配置。本 Wave 产出全部为可编译的 Bean 装配，真实中间件连通性（Milvus/ES/Redis Stack/Redis）验证需 Docker infra 拉起后在集成测试 Wave（06-07）执行。

## Next Phase Readiness

- Wave 2（FAQ/RAG）可直接 `@Autowired @Qualifier` 注入 `milvusVectorStore`/`elasticsearchVectorStore`/`redisStackVectorStore`，以及 `AiClientConfig` 提供的 `ChatClient.Builder`（`mutate()` 追加 `RetrievalAugmentationAdvisor`）
- Wave 3（Agent）可复用 `ModelRouter`（DashScope 主/DeepSeek 备）与 `AuthService.requireCurrentUser()`
- 会话/对话相关 Service 可复用 `ChatMemory`/`MessageChatMemoryAdvisor` Bean
- 遗留说明：`application.yml` 中 `spring.ai.vectorstore.redis.uri` 键位当前未被绑定使用（见 Issues Encountered），后续波次若新增消费方请统一改读 `scs.cache.redis-uri`，避免产生"两处配置真源"混淆
- 无阻塞项

---
*Phase: 06-smart-cs-platform*
*Completed: 2026-07-10*

## Self-Check: PASSED

All 11 created files verified present on disk. All 3 task commit hashes
(`81ff880`, `9000cfb`, `08c304b`) verified present in `git log --oneline --all`.
`mvn -f projects/smart-cs-platform/pom.xml clean compile` succeeded with no errors.
