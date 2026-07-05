---
phase: 04-knowledge-qa-platform
plan: 04
subsystem: api
tags: [jwt, sse, chatclient, conversation, feedback, citation, spring-security]

requires:
  - phase: 04-03
    provides: ChatClient、CitationPostProcessor、documentRetriever Bean
provides:
  - AuthService/AuthController（JWT 登录 + /me）
  - QaService/QaController（同步问答 + SSE message/meta/done/error）
  - ConversationService/Controller（会话 CRUD + Redis 记忆清理）
  - FeedbackService/Controller（qa_feedback 落库）
affects: [04-05-admin, 04-06-test]

tech-stack:
  added: [JwtEncoderParameters, Flux ServerSentEvent, JwtAuthenticationConverter]
  patterns: ["role claim → ROLE_* 供 @PreAuthorize", "会话归属校验 assertOwner", "SSE meta 序列化 QaAnswerVO"]

key-files:
  created:
    - projects/knowledge-qa-platform/src/main/java/com/flywhl/saa/knowledgeqa/model/vo/LoginVO.java
    - projects/knowledge-qa-platform/src/main/java/com/flywhl/saa/knowledgeqa/model/vo/ChatMessageVO.java
  modified:
    - projects/knowledge-qa-platform/src/main/java/com/flywhl/saa/knowledgeqa/service/AuthService.java
    - projects/knowledge-qa-platform/src/main/java/com/flywhl/saa/knowledgeqa/controller/AuthController.java
    - projects/knowledge-qa-platform/src/main/java/com/flywhl/saa/knowledgeqa/service/QaService.java
    - projects/knowledge-qa-platform/src/main/java/com/flywhl/saa/knowledgeqa/controller/QaController.java
    - projects/knowledge-qa-platform/src/main/java/com/flywhl/saa/knowledgeqa/service/ConversationService.java
    - projects/knowledge-qa-platform/src/main/java/com/flywhl/saa/knowledgeqa/controller/ConversationController.java
    - projects/knowledge-qa-platform/src/main/java/com/flywhl/saa/knowledgeqa/service/FeedbackService.java
    - projects/knowledge-qa-platform/src/main/java/com/flywhl/saa/knowledgeqa/controller/FeedbackController.java
    - projects/knowledge-qa-platform/src/main/java/com/flywhl/saa/knowledgeqa/config/SecurityConfig.java

key-decisions:
  - "JWT claims 含 sub/uid/role，JwtAuthenticationConverter 映射 role→ROLE_*"
  - "SSE meta 事件 payload 为 QaAnswerVO JSON（citations + usage，answer 为 null）"
  - "删除会话：RedisChatMemoryRepository.delete + 硬删 qa_message/qa_conversation"

patterns-established:
  - "员工域 Controller 统一 @PreAuthorize hasAnyRole ADMIN/EMPLOYEE"
  - "问答归档：USER/ASSISTANT 双写 qa_message，citations JSONB 快照"

requirements-completed: [REQ-phase-4-knowledge-qa]

duration: 18min
completed: 2026-07-05
---

# Phase 4 Plan 04: 问答域 Controller + Service Summary

**JWT 认证 + 同步/SSE 问答（Citation 独立字段）+ 会话 Redis 清理 + 反馈落库，对齐 api.http 员工域契约**

## Performance

- **Duration:** 18 min
- **Started:** 2026-07-05T15:08:02Z
- **Completed:** 2026-07-05T15:26:02Z
- **Tasks:** 3
- **Files modified:** 11

## Accomplishments

- Auth：Nimbus JwtEncoder 签发 accessToken，POST /api/auth/login、GET /api/auth/me
- Qa：ChatClient + ChatMemory.CONVERSATION_ID，同步 Result\<QaAnswerVO\>；SSE message→meta→done，error 复用 Result
- Conversation：分页列表、历史消息、DELETE 清 Redis + PG
- Feedback：POST /api/qa/feedback 写 qa_feedback，同用户同消息 upsert

## Task Commits

Each task was committed atomically:

1. **Task 1: AuthService + AuthController** - `7d7e09e` (feat)
2. **Task 2: QaService + QaController（同步 + SSE）** - `8e122e2` (feat)
3. **Task 3: Conversation + Feedback 域** - `6515833` (feat)

## Files Created/Modified

- `service/AuthService.java` — 登录校验、JWT 签发、当前用户解析
- `controller/AuthController.java` — /api/auth/login、/api/auth/me
- `service/QaService.java` — ask/stream、消息归档、citation 与 usage
- `controller/QaController.java` — /api/qa/ask、/api/qa/stream
- `service/ConversationService.java` — 会话列表/消息/删除
- `controller/ConversationController.java` — /api/conversations/*
- `service/FeedbackService.java` — 反馈持久化
- `controller/FeedbackController.java` — /api/qa/feedback
- `config/SecurityConfig.java` — JwtAuthenticationConverter role 映射
- `model/vo/LoginVO.java`、`ChatMessageVO.java` — 登录与历史消息 VO

## Decisions Made

- JWT 使用 JwtEncoderParameters.from(claims) 简化 HS256 签发
- 会话删除采用硬删（schema 无 deleted_at），同步清理 Redis 记忆键
- @PreAuthorize 使用 hasAnyRole('ADMIN','EMPLOYEE') 覆盖管理员也可问答

## Deviations from Plan

### Auto-fixed Issues

**1. [Rule 2 - Missing Critical] SecurityConfig JwtAuthenticationConverter**
- **Found during:** Task 1
- **Issue:** JWT 默认不解析 role claim，@PreAuthorize 无法生效
- **Fix:** 配置 authoritiesClaimName=role、authorityPrefix=ROLE_
- **Files modified:** SecurityConfig.java
- **Committed in:** 7d7e09e

**2. [Rule 3 - Blocking] Usage 类包路径**
- **Found during:** Task 2
- **Issue:** org.springframework.ai.chat.model.Usage 不存在
- **Fix:** 改用 org.springframework.ai.chat.metadata.Usage
- **Files modified:** QaService.java
- **Committed in:** 8e122e2

**3. [Rule 2 - Missing Critical] LoginVO / ChatMessageVO**
- **Found during:** Task 1 / Task 3
- **Issue:** 登录响应与历史消息缺少契约 VO
- **Fix:** 新增 LoginVO、ChatMessageVO 及 MessageConverter.toMessageVo
- **Committed in:** 7d7e09e、6515833

## Issues Encountered

None beyond compile-time import fixes.

## User Setup Required

运行时验收需：
- `AI_DASHSCOPE_API_KEY` — 问答与 RAG
- PostgreSQL + Redis + Milvus — 完整问答链路
- `source scripts/setup-env.sh` 后按 `http/api.http` curl

## Next Phase Readiness

- 04-05 admin 五组 API 可独立实现
- 应用 spring-boot:run 需 admin 域或接受员工域单独验收
- 端到端 UAT 待 04-06 测试 + infra 就绪

## Self-Check: PASSED

- FOUND: projects/knowledge-qa-platform/src/main/java/com/flywhl/saa/knowledgeqa/controller/QaController.java
- FOUND: projects/knowledge-qa-platform/src/main/java/com/flywhl/saa/knowledgeqa/service/QaService.java
- FOUND: 7d7e09e, 8e122e2, 6515833
- `mvn -f projects/knowledge-qa-platform/pom.xml compile` — PASSED
- Controller 路径与 api.http 一致（/api/auth/*、/api/qa/*、/api/conversations/*）

---
*Phase: 04-knowledge-qa-platform*
*Completed: 2026-07-05*
