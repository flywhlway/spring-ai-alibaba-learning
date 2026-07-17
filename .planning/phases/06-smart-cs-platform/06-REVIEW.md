---
phase: 06-smart-cs-platform
reviewed: 2026-07-17T14:40:00Z
depth: standard
files_reviewed: 58
files_reviewed_list:
  - projects/smart-cs-platform/db/data.sql
  - projects/smart-cs-platform/db/schema.sql
  - projects/smart-cs-platform/docker-compose.override.yml
  - projects/smart-cs-platform/http/api.http
  - projects/smart-cs-platform/monitor/prometheus.yml
  - projects/smart-cs-platform/pom.xml
  - projects/smart-cs-platform/scripts/uat-smart-cs.sh
  - projects/smart-cs-platform/src/main/java/com/flywhl/saa/smartcs/SmartCsApplication.java
  - projects/smart-cs-platform/src/main/java/com/flywhl/saa/smartcs/admin/controller/DashboardAdminController.java
  - projects/smart-cs-platform/src/main/java/com/flywhl/saa/smartcs/admin/controller/FaqAdminController.java
  - projects/smart-cs-platform/src/main/java/com/flywhl/saa/smartcs/admin/controller/ModelAdminController.java
  - projects/smart-cs-platform/src/main/java/com/flywhl/saa/smartcs/admin/controller/PromptAdminController.java
  - projects/smart-cs-platform/src/main/java/com/flywhl/saa/smartcs/admin/service/DashboardStatsService.java
  - projects/smart-cs-platform/src/main/java/com/flywhl/saa/smartcs/admin/service/ModelAdminService.java
  - projects/smart-cs-platform/src/main/java/com/flywhl/saa/smartcs/agent/CsAgentConfig.java
  - projects/smart-cs-platform/src/main/java/com/flywhl/saa/smartcs/agent/FlowStateExtractor.java
  - projects/smart-cs-platform/src/main/java/com/flywhl/saa/smartcs/config/AiClientConfig.java
  - projects/smart-cs-platform/src/main/java/com/flywhl/saa/smartcs/config/ChatMemoryConfig.java
  - projects/smart-cs-platform/src/main/java/com/flywhl/saa/smartcs/config/ConfigurableModelRouter.java
  - projects/smart-cs-platform/src/main/java/com/flywhl/saa/smartcs/config/ElasticsearchVectorStoreConfig.java
  - projects/smart-cs-platform/src/main/java/com/flywhl/saa/smartcs/config/MilvusVectorStoreConfig.java
  - projects/smart-cs-platform/src/main/java/com/flywhl/saa/smartcs/config/ModelProfileNacosPublisher.java
  - projects/smart-cs-platform/src/main/java/com/flywhl/saa/smartcs/config/ObservabilityConfig.java
  - projects/smart-cs-platform/src/main/java/com/flywhl/saa/smartcs/config/OpenApiConfig.java
  - projects/smart-cs-platform/src/main/java/com/flywhl/saa/smartcs/config/RedisChatMemoryRepository.java
  - projects/smart-cs-platform/src/main/java/com/flywhl/saa/smartcs/config/RedisStackCacheConfig.java
  - projects/smart-cs-platform/src/main/java/com/flywhl/saa/smartcs/config/ScsProperties.java
  - projects/smart-cs-platform/src/main/java/com/flywhl/saa/smartcs/config/SecurityConfig.java
  - projects/smart-cs-platform/src/main/java/com/flywhl/saa/smartcs/controller/AuthController.java
  - projects/smart-cs-platform/src/main/java/com/flywhl/saa/smartcs/controller/ChatController.java
  - projects/smart-cs-platform/src/main/java/com/flywhl/saa/smartcs/controller/HumanHandoffController.java
  - projects/smart-cs-platform/src/main/java/com/flywhl/saa/smartcs/controller/TicketController.java
  - projects/smart-cs-platform/src/main/java/com/flywhl/saa/smartcs/model/dto/ChatRequest.java
  - projects/smart-cs-platform/src/main/java/com/flywhl/saa/smartcs/model/dto/HandoffStartRequest.java
  - projects/smart-cs-platform/src/main/java/com/flywhl/saa/smartcs/model/dto/TicketTransitionRequest.java
  - projects/smart-cs-platform/src/main/java/com/flywhl/saa/smartcs/prompt/PromptPublishService.java
  - projects/smart-cs-platform/src/main/java/com/flywhl/saa/smartcs/prompt/PromptTemplateProvider.java
  - projects/smart-cs-platform/src/main/java/com/flywhl/saa/smartcs/rag/FaqEtlPipeline.java
  - projects/smart-cs-platform/src/main/java/com/flywhl/saa/smartcs/rag/HybridSearchService.java
  - projects/smart-cs-platform/src/main/java/com/flywhl/saa/smartcs/rag/RagPipelineFactory.java
  - projects/smart-cs-platform/src/main/java/com/flywhl/saa/smartcs/repository/CsTicketRepository.java
  - projects/smart-cs-platform/src/main/java/com/flywhl/saa/smartcs/service/AuthService.java
  - projects/smart-cs-platform/src/main/java/com/flywhl/saa/smartcs/service/ChatService.java
  - projects/smart-cs-platform/src/main/java/com/flywhl/saa/smartcs/service/CsOrchestratorService.java
  - projects/smart-cs-platform/src/main/java/com/flywhl/saa/smartcs/service/FaqAnswerService.java
  - projects/smart-cs-platform/src/main/java/com/flywhl/saa/smartcs/service/SemanticCacheService.java
  - projects/smart-cs-platform/src/main/java/com/flywhl/saa/smartcs/service/TicketService.java
  - projects/smart-cs-platform/src/main/java/com/flywhl/saa/smartcs/tool/FaqTool.java
  - projects/smart-cs-platform/src/main/java/com/flywhl/saa/smartcs/tool/HandoffTools.java
  - projects/smart-cs-platform/src/main/java/com/flywhl/saa/smartcs/tool/OrderTool.java
  - projects/smart-cs-platform/src/main/java/com/flywhl/saa/smartcs/tool/TicketTools.java
  - projects/smart-cs-platform/src/main/java/com/flywhl/saa/smartcs/tool/ToolSecuritySupport.java
  - projects/smart-cs-platform/src/main/resources/application.yml
  - projects/smart-cs-platform/src/main/resources/prompts/faq-answer-system.st
  - projects/smart-cs-platform/src/main/resources/prompts/query-rewrite.st
  - projects/smart-cs-platform/src/test/java/com/flywhl/saa/smartcs/support/ScsPostgresRedisITBase.java
  - projects/smart-cs-platform/src/test/resources/application-it.yml
  - projects/smart-cs-platform/src/test/resources/application-test.yml
findings:
  critical: 3
  warning: 5
  info: 3
  total: 11
status: issues_found
---

# Phase 06: Code Review Report

**Reviewed:** 2026-07-17T14:40:00Z
**Depth:** standard
**Files Reviewed:** 58
**Status:** issues_found

## Summary

对 `projects/smart-cs-platform/` 在 Phase 06（06-01～06-07 SUMMARY key-files）范围内的生产源码做了 standard 深度对抗审查，重点覆盖认证/RBAC、工单与 HITL、编排网关与 Tool 权限。整体脚手架、状态机与管理端 ADMIN 隔离写得较完整，但会话→人工接管链路与工单数据隔离存在可复现的正确性/授权缺陷，建议在合入前优先修复 Critical 项。

## Narrative Findings (AI reviewer)

## Critical Issues

### CR-01: Chat 路径触发 HITL 中断后无法 approve，且工单未升至 PENDING_HUMAN

**File:** `projects/smart-cs-platform/src/main/java/com/flywhl/saa/smartcs/service/ChatService.java:78-87`（stream 同源：`115-123`）
**Issue:** `CsOrchestratorService.invoke` 在 HITL 中断时返回带 `InterruptionMetadata` 的结果，但 `ChatService.ask/stream` 仅落库助手消息并提示调用 `/api/handoff/approve`，**既未**把 `InterruptionMetadata` 写入 `HumanHandoffController.pendingByThread`，**也未**调用 `TicketService.transitionToPendingHuman`。HITL 钩子在工具执行前中断，因此 `HandoffTools.requestHumanHandoff` 不会跑，工单状态机也不会推进。坐席随后 `POST /api/handoff/approve` 会因 pending 表为空而 404（仅 `/api/handoff/start` 路径完整）。
**Fix:** 将 pending 注册与工单升级抽成共享服务，在 chat 中断分支复用 start 路径逻辑：

```java
// ChatService 中断分支示意
if (result.interrupted()) {
    hitlPendingStore.put(conversationId, result.interruptionMetadata());
    ticketService.transitionToPendingHuman(
            conversationId, user.getId(), request.question(), user.getRole().name());
    // ... 再返回 interrupt 提示
}
```

同时保证 `approve` 与 chat 路由使用同一 `ReactAgent`/`MemorySaver` 与相同 `threadId`。

### CR-02: 工单查询/催单存在 IDOR（客户可读/催任意工单）

**File:** `projects/smart-cs-platform/src/main/java/com/flywhl/saa/smartcs/controller/TicketController.java:60-66`
**Issue:** `GET /api/tickets/{no}` 对 CUSTOMER/AGENT/ADMIN 开放，但返回前不校验 `ticket.customerId` 是否等于当前用户。任意登录客户只要知道（或枚举）工单号即可读取他人摘要与状态。同缺陷也存在于 Tool 层：`TicketTools.queryTicketByNo` / `urgeTicket`（`TicketTools.java:44-64`）无归属校验，模型传入任意工单号即可越权查询/催单。
**Fix:**

```java
@GetMapping("/{no}")
public Result<TicketVO> getByNo(@PathVariable("no") String ticketNo) {
    SysUser user = authService.requireCurrentUser();
    CsTicket ticket = ticketService.findByTicketNo(ticketNo)
            .orElseThrow(() -> new BizException(CommonResultCode.NOT_FOUND, "工单不存在：" + ticketNo));
    assertTicketAccess(ticket, user); // CUSTOMER 必须 customerId 匹配；AGENT/ADMIN 放行
    return Result.ok(ticketConverter.toVo(ticket));
}
```

Tool 路径同样在 `queryTicketByNo`/`urgeTicket` 中用 `userIdOf` + 角色做归属校验。

### CR-03: Tool 身份回退为 CUSTOMER 且未注入 ToolContext，存在越权默认与空 customerId 风险

**File:** `projects/smart-cs-platform/src/main/java/com/flywhl/saa/smartcs/tool/ToolSecuritySupport.java:21-33`
**Issue:** `roleOf` 在无法解析 JWT/`ToolContext` 时**默认为 `"CUSTOMER"`**，使允许 CUSTOMER 的 Tool（查单/建单/催单/订单）在无身份时仍通过 `requireRole`。`CsOrchestratorService` 未向 `ToolContext` 注入 `userId`/`role`，完全依赖 `SecurityContextHolder`。`ChatService.stream` 经 `Mono.fromCallable` 切线程后 ThreadLocal 安全上下文可能丢失；此时 `userIdOf` 返回 `null`，而 `cs_ticket.customer_id` 为 NOT NULL——建单会在持久化阶段失败，或在部分路径留下错误归属。这与威胁登记 T-06-06「Tool 越权校验」意图相反。
**Fix:**

```java
public static String roleOf(ToolContext toolContext) {
    // ... 已有解析 ...
    throw new BizException(CommonResultCode.UNAUTHORIZED, "Tool 调用缺少身份上下文");
    // 禁止 return "CUSTOMER";
}

public static Long userIdOf(ToolContext toolContext) {
    // ... 已有解析 ...
    throw new BizException(CommonResultCode.UNAUTHORIZED, "Tool 调用缺少 userId");
}
```

并在编排入口显式注入：`RunnableConfig`/`ToolContext` 放入当前 `uid`+`role`；SSE 路径用 `SecurityContext` 捕获或 `Hooks`/`contextWrite` 传播。

## Warnings

### WR-01: 创建工单/发起接管未校验 conversation 归属

**File:** `projects/smart-cs-platform/src/main/java/com/flywhl/saa/smartcs/controller/TicketController.java:47-57`
**Issue:** `POST /api/tickets` 与 `HumanHandoffController.start`（`71-87`）接受客户端传入的 `conversationId`，未像 `ChatService.assertAccess` 那样校验 CUSTOMER 只能操作自己的会话。客户可对他人 `conversation_id` 建单或触发升级；Agent Tool 侧同样信任模型传入的 `conversationId` 前缀标记。
**Fix:** 在创建/升级前 `ensureConversation` 或显式 `assertAccess`；Tool 侧优先使用服务端会话上下文中的 conversationId，而非仅依赖模型抄写的标记。

### WR-02: 工单号生成存在并发碰撞窗口

**File:** `projects/smart-cs-platform/src/main/java/com/flywhl/saa/smartcs/service/TicketService.java:178-183`
**Issue:** `countByTicketNoStartingWith(prefix) + 1` 非原子；并发建单可产生相同序号，触发 `ticket_no` UNIQUE 失败或（在重试缺失时）间歇性 500。
**Fix:** 使用 DB sequence、`SELECT … FOR UPDATE` 日计数表，或捕获唯一约束冲突后重试。

### WR-03: 多智能体路径未使用 ConfigurableModelRouter（管理端模型配置对 Agent 无效）

**File:** `projects/smart-cs-platform/src/main/java/com/flywhl/saa/smartcs/agent/CsAgentConfig.java:45-214`
**Issue:** 所有 ReactAgent/LlmRoutingAgent 硬编码注入 `dashScopeChatModel`。`ConfigurableModelRouter.routeForScene` 仅被 `AiClientConfig` 的 FAQ `ChatClient.Builder` 使用。后台 `model_profile` 对 BUSINESS/TICKET/路由 Agent 不生效，与 06-06「按 scene 选模型」交付预期不一致。
**Fix:** Agent Bean 通过 `configurableModelRouter.routeForScene(SCENE_*)` 取 `ChatModel`，或在路由层按 scene 动态选择。

### WR-04: SSE `/api/chat/stream` 将完整问题放在 query string

**File:** `projects/smart-cs-platform/src/main/java/com/flywhl/saa/smartcs/controller/ChatController.java:48-52`
**Issue:** 用户问题经 GET 查询参数传输，易进入访问日志、代理日志与浏览器历史，造成 PII/业务内容泄露。
**Fix:** 改为 `POST` + body（可仍返回 `text/event-stream`），或至少对日志脱敏。

### WR-05: Dashboard Token 成本对 gen_ai 计数器一律按 input 单价估算

**File:** `projects/smart-cs-platform/src/main/java/com/flywhl/saa/smartcs/admin/service/DashboardStatsService.java:117-124`
**Issue:** `sumGenAiUsageCost` 把所有 `gen_ai.client.token.usage` counter 都乘以 `pricePer1kInputTokens`，未区分 input/output（通常靠 tag）。指标路径开启时成本会系统性偏差。
**Fix:** 按 meter tag（`token.type`/`input`/`output`）分别乘对应单价；无法区分时回退消息表估算。

## Info

### IN-01: JWT 与演示账号使用开发默认密钥/{noop} 密码

**File:** `projects/smart-cs-platform/src/main/resources/application.yml:129`；`ScsProperties.java:62-65`；`db/data.sql:11-15`
**Issue:** 默认 `SCS_JWT_SECRET` 与 `{noop}admin123` 等为明确的 dev-only 设计，文档已说明。若部署文档未强制覆盖，易被误用于共享环境。
**Fix:** 启动时对默认 JWT secret / `{noop}` 打 WARN；生产 profile 拒绝默认值。

### IN-02: `/actuator/prometheus` permitAll

**File:** `projects/smart-cs-platform/src/main/java/com/flywhl/saa/smartcs/config/SecurityConfig.java:63`
**Issue:** Prometheus 端点匿名可读，教学演示可接受，共享网络会暴露调用量与延迟。
**Fix:** 生产改为网络策略限制或 `authenticated()`/`hasRole('ADMIN')`。

### IN-03: HITL pending 使用进程内 ConcurrentHashMap

**File:** `projects/smart-cs-platform/src/main/java/com/flywhl/saa/smartcs/controller/HumanHandoffController.java:57-60`
**Issue:** 代码已注明重启丢失；多实例下 approve 无法命中。属已知演示限制，修复 CR-01 时应一并考虑 Redis/DB 持久化。
**Fix:** 将 pending `InterruptionMetadata`（或可恢复句柄）外置存储。

---

_Reviewed: 2026-07-17T14:40:00Z_
_Reviewer: Claude (gsd-code-reviewer)_
_Depth: standard_
