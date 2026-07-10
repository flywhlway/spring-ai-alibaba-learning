---
phase: 06-smart-cs-platform
plan: 04
subsystem: agent
tags: [agent-framework, llm-routing-agent, react-agent, supervisor, human-in-the-loop, parallel-agent, spring-ai-alibaba]

# Dependency graph
requires:
  - phase: 06-smart-cs-platform (Wave 1 / Plan 02)
    provides: AiClientConfig（dashScopeChatModel 经父 BOM autoconfig 可注入）、ScsProperties、SecurityConfig（JWT role claim）
  - phase: 06-smart-cs-platform (Wave 2 / Plan 03)
    provides: FaqAnswerService.answer(query)（缓存→混合检索→RAG 全链路，返回 ChatAnswerVO）
provides:
  - CsAgentConfig：LlmRoutingAgent（cs-intent-router）四分支路由 + ReactAgent Supervisor（business-supervisor，AgentTool.create 调度 order/afterSales/techSupport）+ ticket-agent + human-escalation-agent（HumanInTheLoopHook.approvalOn 拦截 requestHumanHandoff）+ faq-parallel-context（ParallelAgent 演示并行子智能体）
  - Tool 族：ToolSecuritySupport（ToolContext 越权校验）、OrderTool（演示订单查询）、TicketTools（建单/查单/催单）、HandoffTools（人工升级）、FaqTool（包装 FaqAnswerService）
  - TicketService：Wave 3 最小实现（create/find/urge/createOrEscalate），完整状态机留待 06-05
  - CsOrchestratorService：conversationId 作 RunnableConfig.threadId 调用 csIntentRouter，识别 InterruptionMetadata 中断态，routeAgent 归一化 FAQ/BUSINESS/TICKET/HUMAN 常量
affects: [06-smart-cs-platform-05-conversation-ticket, 06-smart-cs-platform-06-admin, 06-smart-cs-platform-07-testing]

# Tech tracking
tech-stack:
  added: []
  patterns:
    - "LlmRoutingAgent 顶层四分支路由 + ReactAgent Supervisor（AgentTool.create 而非 AgentTool.from）调度领域子 Agent，禁用教程伪 API SupervisorAgent"
    - "HumanInTheLoopHook.approvalOn(\"requestHumanHandoff\", ...) 拦截高风险 Tool，禁用 interruptBefore；humanEscalationAgent 挂 MemorySaver 支持后续波次坐席 approve/resume"
    - "conversationId 经消息前缀 [conversationId=xxx] 标记透传给 ticket-agent/human-escalation-agent 的 @Tool 方法（RunnableConfig.threadId 本身不对 Tool 可见，需显式标记供 LLM 原样传参）"
    - "NodeOutput.agent() 归一化为 4 类路由常量（RouteAgent.FAQ/BUSINESS/TICKET/HUMAN），供 cs_message.route_agent 落库（Wave 4 ChatService 消费）"
    - "csIntentRouter.invokeAndGetOutput 而非 invoke：可同时获得 NodeOutput（含 agent() 归属信息）与识别 InterruptionMetadata 中断态，语义等价于 37-demo HitlController 模式但用于顶层路由 Agent"

key-files:
  created:
    - projects/smart-cs-platform/src/main/java/com/flywhl/saa/smartcs/tool/ToolSecuritySupport.java
    - projects/smart-cs-platform/src/main/java/com/flywhl/saa/smartcs/tool/OrderTool.java
    - projects/smart-cs-platform/src/main/java/com/flywhl/saa/smartcs/tool/TicketTools.java
    - projects/smart-cs-platform/src/main/java/com/flywhl/saa/smartcs/tool/HandoffTools.java
    - projects/smart-cs-platform/src/main/java/com/flywhl/saa/smartcs/tool/FaqTool.java
    - projects/smart-cs-platform/src/main/java/com/flywhl/saa/smartcs/service/TicketService.java
    - projects/smart-cs-platform/src/main/java/com/flywhl/saa/smartcs/agent/CsAgentConfig.java
    - projects/smart-cs-platform/src/main/java/com/flywhl/saa/smartcs/agent/FlowStateExtractor.java
    - projects/smart-cs-platform/src/main/java/com/flywhl/saa/smartcs/service/CsOrchestratorService.java
  modified: []

key-decisions:
  - "TicketService（最小实现）落在 service/ 包而非 frontmatter files_modified 精确枚举之外：Task 1 <action> 明确要求「本任务可实现 TicketService 最小 create/find 供 Tool 使用」，TicketTools/HandoffTools 编译期强依赖该 Service；files_modified 对 tool/ 与 agent/ 是目录级声明，service/ 仅显式列出 CsOrchestratorService.java，故此文件视为任务文本的必要实现产出而非越权扩域，Wave 5（06-05）将补全完整状态机校验"
  - "conversationId 采用消息前缀 [conversationId=xxx] 标记透传给需要该参数的 @Tool 方法：RunnableConfig.threadId(conversationId) 仅供 Graph 层做会话隔离/检查点，不会自动注入到 ToolContext 或 @Tool 参数；ticket-agent/human-escalation-agent 的 systemPrompt 显式指示模型从用户消息开头提取并原样透传，CsOrchestratorService.invoke 负责拼接该标记"
  - "CsOrchestratorService 改用 csIntentRouter.invokeAndGetOutput（返回 Optional<NodeOutput>）而非 plan 示例代码片段中的 invoke（返回 Optional<OverAllState>）：NodeOutput 额外暴露 agent() 方法（产出最终结果的子 Agent 名）与可直接 instanceof 判断的 InterruptionMetadata，二者是 routeAgent 归一化与 HITL 中断态识别的唯一可靠数据源（经 JAR 字节码验证，OverAllState 本身不暴露路由决策），语义仍完全覆盖 must_haves 的 threadId 调用契约"
  - "routeAgent 由 NodeOutput.agent() 返回的具体子 Agent bean 名（如 order-agent/aftersales-agent/techsupport-agent）归一化映射为 4 类常量 FAQ/BUSINESS/TICKET/HUMAN，而非直接透传原始 bean 名：满足 acceptance criteria「常量枚举 FAQ/BUSINESS/TICKET/HUMAN」的显式约束，Wave 4 ChatService 落库 cs_message.route_agent 时可直接使用该常量，无需二次归一化逻辑"
  - "afterSalesAgent/techSupportAgent 挂载 TicketTools（而非各自专属领域 Tool）：Task 1 的 acceptance_criteria 仅要求 ToolSecuritySupport/OrderTool/TicketTools/HandoffTools 四个 Tool 类，售后/技术支持领域未单独立项工具集；两个子 Agent 若无法当场解决问题，通过 createTicket 建单留痕是合理的最小可行路径，专属领域 Tool（如退换货审批、远程诊断）留待后续波次按需扩展"
  - "faqParallelContext（ParallelAgent）未强行接入 csIntentRouter 顶层路由 subAgents 列表：must_haves 仅要求 csIntentRouter 路由 FAQ/business-supervisor/ticket-agent/human-escalation 四分支，acceptance_criteria 对 ParallelAgent 标注为「可选挂载」；ParallelAgent 产出的 mergedContext 与 ReactAgent 单次查询语义不兼容（AgentTool.create 仅接受 ReactAgent），故发布为独立 Bean 满足『并行子智能体』架构模式验收，供后续波次（SSE 前置增强）按需消费"

patterns-established:
  - "Pattern: @Tool 方法访问会话级上下文（conversationId）时，若 RunnableConfig.threadId 不可达，改用消息前缀标记 + systemPrompt 显式指示模型透传，Service 层负责拼接/剥离标记"
  - "Pattern: 顶层 FlowAgent（LlmRoutingAgent/SequentialAgent 等）编排结果的『谁处理了这次请求』信息，统一从 NodeOutput.agent() 读取并归一化为业务常量，而非解析 messages 列表猜测来源"

requirements-completed: [REQ-phase-6-smart-cs]

# Metrics
duration: 75min
completed: 2026-07-10
---

# Phase 6 Plan 4: 智能客服多智能体编排（LlmRoutingAgent + Supervisor + HITL）Summary

**CsAgentConfig 落地 LlmRoutingAgent 四分支路由 + ReactAgent Supervisor（AgentTool.create 调度订单/售后/技术子 Agent）+ HumanInTheLoopHook.approvalOn 人工升级 + ParallelAgent 并行检索演示；Tool 族（OrderTool/TicketTools/HandoffTools/FaqTool）+ ToolSecuritySupport 越权校验；CsOrchestratorService 以 conversationId 为 threadId 统一调用入口并识别 HITL 中断态，`mvn clean compile test` 全绿，禁用 Agent API grep 零命中**

## Performance

- **Duration:** 约 75 分钟
- **Started:** 2026-07-10（本次执行会话）
- **Completed:** 2026-07-10T12:17:36Z
- **Tasks:** 3/3
- **Files modified:** 9（全部新建）

## Accomplishments

- `ToolSecuritySupport`：从 `ToolContext`/`SecurityContext` 解析 `userId`/`role`，`requireRole` 越权校验（威胁登记 T-06-06 缓解）；`OrderTool.queryOrderStatus` 演示级固定数据查询；`TicketTools`（createTicket/queryTicketByNo/urgeTicket）+ `HandoffTools.requestHumanHandoff` 委托新增 `TicketService`（Wave 3 最小实现：create/find/urge/createOrEscalate，完整状态机留待 06-05）
- `CsAgentConfig` 全 Bean 图落地 JAR 真 API：`csIntentRouter`（`LlmRoutingAgent`）路由 `faq-agent`/`business-supervisor`/`ticket-agent`/`human-escalation-agent` 四分支；`businessSupervisor`（`ReactAgent` + `AgentTool.create` 调度 `order-agent`/`aftersales-agent`/`techsupport-agent`）；`humanEscalationAgent` 挂 `HumanInTheLoopHook.builder().approvalOn("requestHumanHandoff", ...)` + `MemorySaver`；`faqParallelContext`（`ParallelAgent`，`knowledgeBaseAgent`+`ticketHistoryAgent`，`mergeOutputKey=mergedContext`）演示并行子智能体模式；`FlowStateExtractor.extractText` 跨包公开供 Service 层复用
- `FaqTool` 包装 `FaqAnswerService.answer` 为 `@Tool`，供 `faqAgent` 挂载，避免 ReactAgent 绕过既有语义缓存/混合检索/RAG 链路凭空作答
- `CsOrchestratorService.invoke(conversationId, question)`：`conversationId` 作 `RunnableConfig.threadId`（为空时生成 UUID，禁止自增）；消息前缀 `[conversationId=xxx]` 标记透传给需要该参数的 Tool；`csIntentRouter.invokeAndGetOutput` 返回的 `NodeOutput` 若为 `InterruptionMetadata` 则标记 `interrupted=true` 并透传，供 Wave 4 HITL Controller 消费；`NodeOutput.agent()` 归一化为 `RouteAgent.FAQ/BUSINESS/TICKET/HUMAN` 常量；`GraphRunnerException` 转 `BizException(INTERNAL_ERROR)`
- `mvn -f projects/smart-cs-platform/pom.xml clean compile test` 全绿；`rg` 全项目扫描禁用 API（`SupervisorAgent`/`AgentTool.from`/`interruptBefore`/`PromptChatMemoryAdvisor`/`FunctionCallback`）零命中；`LlmRoutingAgent`（4 处）、`AgentTool.create`（3 处）、`HumanInTheLoopHook.builder()...approvalOn`（1 处）均存在

## Task Commits

Each task was committed atomically:

1. **Task 1: Tool 族 + ToolSecuritySupport + TicketService 最小实现** - `c1ad23f` (feat)
2. **Task 2: CsAgentConfig 多智能体编排 + FlowStateExtractor** - `acc51a4` (feat，含 Javadoc 禁用词门禁误判修正)
3. **Task 3: CsOrchestratorService 编排统一入口** - `0b2210c` (feat)

**Plan metadata:** (this commit, docs: complete plan)

## Files Created/Modified

- `projects/smart-cs-platform/src/main/java/com/flywhl/saa/smartcs/tool/ToolSecuritySupport.java` - ToolContext/SecurityContext 越权校验支持
- `projects/smart-cs-platform/src/main/java/com/flywhl/saa/smartcs/tool/OrderTool.java` - 演示级订单物流查询 `@Tool`
- `projects/smart-cs-platform/src/main/java/com/flywhl/saa/smartcs/tool/TicketTools.java` - createTicket/queryTicketByNo/urgeTicket `@Tool`
- `projects/smart-cs-platform/src/main/java/com/flywhl/saa/smartcs/tool/HandoffTools.java` - requestHumanHandoff `@Tool`（HITL 拦截目标）
- `projects/smart-cs-platform/src/main/java/com/flywhl/saa/smartcs/tool/FaqTool.java` - 包装 FaqAnswerService.answer 的 `@Tool`
- `projects/smart-cs-platform/src/main/java/com/flywhl/saa/smartcs/service/TicketService.java` - 工单最小服务（create/find/urge/createOrEscalate）
- `projects/smart-cs-platform/src/main/java/com/flywhl/saa/smartcs/agent/CsAgentConfig.java` - 全 Agent Bean 图（Routing+Supervisor+HITL+Parallel）
- `projects/smart-cs-platform/src/main/java/com/flywhl/saa/smartcs/agent/FlowStateExtractor.java` - OverAllState 助手文本提取
- `projects/smart-cs-platform/src/main/java/com/flywhl/saa/smartcs/service/CsOrchestratorService.java` - 编排统一调用入口

## Decisions Made

详见 frontmatter `key-decisions`：TicketService 归属判定、conversationId 消息前缀透传方案、`invokeAndGetOutput` 替代 `invoke` 的技术依据（含 JAR 字节码验证）、routeAgent 归一化映射、afterSales/techSupport 复用 TicketTools、faqParallelContext 独立发布不强行接入顶层路由。

## Deviations from Plan

### Auto-fixed Issues

**1. [Rule 1 - Bug] CsAgentConfig/HandoffTools Javadoc 直写禁用 API 类名触发 grep 门禁误判**
- **Found during:** Task 2（禁用 API grep 验证）
- **Issue:** Javadoc 中直接拼写了禁用 API 名称 `SupervisorAgent`/`AgentTool.from`/`interruptBefore`（用于说明"本类不使用该已废弃写法"），被门禁 `rg 'SupervisorAgent|AgentTool\.from|interruptBefore'` 误判为命中，与 06-02-SUMMARY 记录的 `ChatMemoryConfig` 案例同源
- **Fix:** 改写 Javadoc 为不含字面 token 的等价说明，保留原有"禁用教程伪 API"意图
- **Files modified:** `CsAgentConfig.java`、`HandoffTools.java`
- **Verification:** `rg -v '^#' .../src/main/java | rg -c 'SupervisorAgent|AgentTool\.from|interruptBefore'` 退出码 1（零命中，与已知 ripgrep 零匹配静默退出行为一致）
- **Committed in:** `acc51a4`（Task 2 commit）

---

**Total deviations:** 1 auto-fixed（1 bug，与 06-02 同源的文档措辞门禁误判）
**Impact on plan:** 仅修正 Javadoc 措辞，未改变任何运行时行为，不构成范围蔓延。

## Issues Encountered

- SAA 1.1.2.2 `LlmRoutingAgent` 顶层 `invoke()`/`OverAllState` 不直接暴露"最终由哪个子 Agent 产出结果"的公开状态键（经反编译 `RoutingNode`/`OverAllState` 字节码确认：路由决策仅用于内部 `MultiCommand` 图跳转，不写回可查询的 state 变量）；改用 `Agent.invokeAndGetOutput` 返回的 `NodeOutput.agent()`（公开 API，`spring-ai-alibaba-graph-core` 1.1.2.2 已验证存在）作为唯一可靠数据源，语义等价且更贴合 `InterruptionMetadata`（`NodeOutput` 子类）中断态识别需求
- conversationId 无法通过 `RunnableConfig.threadId` 自动注入到 `@Tool` 方法的 `ToolContext`（二者是 Graph 层与 Tool 调用层两套独立机制），需要 `CsOrchestratorService` 显式拼接 `[conversationId=xxx]` 消息前缀 + 各子 Agent `systemPrompt` 指示模型透传，非计划原文逐字给出的实现细节，但与 must_haves「`RunnableConfig.threadId(conversationId)`」的调用契约完全一致

## User Setup Required

None - 无需外部服务人工配置。本 Wave 产出全部为可编译、单测通过的 Agent Bean 装配与 Service 层代码；真实模型调用（DashScope 路由决策、Agent 对话）需 `AI_DASHSCOPE_API_KEY` 环境变量，在 Wave 7（06-07 测试/UAT）或人工 UAT 阶段验证。

## Next Phase Readiness

- Wave 4（会话/工单）可直接注入 `CsOrchestratorService`，包装为 SSE 流式端点（`ChatController`/`ChatService`），并在收到 `interrupted=true` 时调用 `TicketService`/`HumanHandoffController` 落 `PENDING_HUMAN` 与坐席 approve/resume 恢复
- `CsOrchestratorService.RouteAgent` 常量（FAQ/BUSINESS/TICKET/HUMAN）可直接写入 `cs_message.route_agent` 列
- `TicketService` 当前仅提供最小 create/find/urge/createOrEscalate，Wave 5（06-05）需补齐完整状态机校验（`ALLOWED_TRANSITIONS` 合法转移图、`HUMAN_HANDLING`→`RESOLVED`→`CLOSED` 流转、坐席 approve 恢复 `RunnableConfig.addHumanFeedback().resume()`）
- `faqParallelContext`（`ParallelAgent`）已发布为 Bean，若后续波次需要"FAQ 前置并行增强"可直接注入消费；当前独立于主路由链路，无阻塞
- 无阻塞项

---
*Phase: 06-smart-cs-platform*
*Completed: 2026-07-10*

## Self-Check: PASSED

All 9 created files verified present on disk (ToolSecuritySupport.java / OrderTool.java /
TicketTools.java / HandoffTools.java / FaqTool.java / TicketService.java / CsAgentConfig.java /
FlowStateExtractor.java / CsOrchestratorService.java). All 3 task commit hashes
(`c1ad23f`, `acc51a4`, `0b2210c`) verified present in `git log --oneline --all`.
`mvn -f projects/smart-cs-platform/pom.xml clean compile test` succeeded with no errors.
