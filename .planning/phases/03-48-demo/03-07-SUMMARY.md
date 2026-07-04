---
phase: 03-48-demo
plan: 07
subsystem: mcp
tags: [mcp, streamable-http, mcp-server, mcp-client, bearer-auth, nacos, spring-ai-1.1.2]

requires:
  - phase: 03-48-demo
    provides: common Result/GlobalExceptionHandler、教程 12-MCP 规格、saa-conventions
provides:
  - 31-mcp-server-demo Streamable HTTP @McpTool Server（18031）
  - 32-mcp-client-demo SyncMcpToolCallbackProvider Client（18032）
  - 33-mcp-auth-demo Bearer TransportContextExtractor（18033）
  - 34-mcp-nacos-demo 双模块 Nacos 注册发现（18034/18134）
affects: [03-08, agent demos, enterprise tool interoperability]

tech-stack:
  added:
    - spring-ai-starter-mcp-server-webmvc
    - spring-ai-starter-mcp-client
    - spring-ai-alibaba-starter-mcp-registry
    - spring-ai-alibaba-starter-mcp-distributed
    - org.springaicommunity.mcp.annotation.McpTool
  patterns:
    - MCP Server protocol STREAMABLE + endpoint /mcp
    - Client defaultToolCallbacks(SyncMcpToolCallbackProvider)
    - Bearer via McpTransportContextExtractor（传输层，非模型参数）
    - Nacos 按 service-name 发现，Client = Server+100

key-files:
  created:
    - examples/31-mcp-server-demo/
    - examples/32-mcp-client-demo/
    - examples/33-mcp-auth-demo/
    - examples/34-mcp-nacos-demo/order-mcp-server/
    - examples/34-mcp-nacos-demo/office-assistant-client/
  modified: []

key-decisions:
  - "@McpTool 使用 org.springaicommunity.mcp.annotation（1.1.2 真源），非教程虚构的 tool.annotation 包"
  - "ChatClient 挂载 MCP 工具用 defaultToolCallbacks，非教程 defaultTools"
  - "Nacos starter 用 mcp-registry / mcp-distributed（1.1.2.2 坐标），非教程 nacos-mcp-server/client"
  - "Client 连接拆成 url=http://localhost:18031 + endpoint=/mcp"

patterns-established:
  - "MCP Server：protocol STREAMABLE + @McpTool Service bean"
  - "MCP Auth：覆盖 WebMvcStreamableServerTransportProvider 注入 contextExtractor"
  - "MCP Nacos：register.enabled / client.enabled + streamable.connections.*.service-name"

requirements-completed: [REQ-phase-3-demos]

duration: 13min
completed: 2026-07-04
---

# Phase 03 Plan 07: MCP 四件套（31–34）Summary

**MCP Server/Client/Bearer 鉴权/Nacos 注册发现四件套，端口 18031–18034/18134，全部 compile 绿**

## Performance

- **Duration:** 13 min
- **Started:** 2026-07-04T14:42:18Z
- **Completed:** 2026-07-04T14:54:57Z
- **Tasks:** 4/4
- **Files modified:** 35（新建）

## Accomplishments

- 31：`@McpTool` Streamable HTTP Server，冒烟 IT 校验 context + McpTool bean，README 标注生产须鉴权
- 32：仅连接本机 `http://localhost:18031` + `/mcp`，`SyncMcpToolCallbackProvider` + `/ask`
- 33：`McpTransportContextExtractor` 提取 Bearer，`ProtectedTools` 校验 `demo-secret`
- 34：`order-mcp-server`(18034) 注册 `order-service-mcp`；`office-assistant-client`(18134) 按服务名发现

## Task Commits

1. **Task 1: 新建 31-mcp-server-demo** - `61684a1` (feat)
2. **Task 2: 新建 32-mcp-client-demo** - `cd2baa2` (feat)
3. **Task 3: 新建 33-mcp-auth-demo** - `e91798f` (feat)
4. **Task 4: 新建 34-mcp-nacos-demo（双模块）** - `cb03cb4` (feat)

**Plan metadata:** `26e2b23` (docs: complete plan)

## Files Created/Modified

- `examples/31-mcp-server-demo/` — Streamable HTTP MCP Server + IT
- `examples/32-mcp-client-demo/` — MCP Client 消费 31
- `examples/33-mcp-auth-demo/` — Bearer 传输层鉴权
- `examples/34-mcp-nacos-demo/order-mcp-server/` — Nacos 注册 Server
- `examples/34-mcp-nacos-demo/office-assistant-client/` — Nacos 发现 Client
- `examples/34-mcp-nacos-demo/README.md` — 启动顺序与安全提示

## Decisions Made

- **注解包**：Spring AI 1.1.2 实际为 `org.springaicommunity.mcp.annotation.McpTool`；教程写的 `org.springframework.ai.tool.annotation.McpTool` 不存在；禁止使用 2.0 的 `org.springframework.ai.mcp.annotation`
- **Client API**：`ChatClient.Builder.defaultToolCallbacks(ToolCallbackProvider...)` 等价教程 `defaultTools(SyncMcpToolCallbackProvider)`
- **Nacos 坐标**：SAA 1.1.2.2 使用 `spring-ai-alibaba-starter-mcp-registry`（Server）与 `spring-ai-alibaba-starter-mcp-distributed`（Client）；配置键仍为 `spring.ai.alibaba.mcp.nacos.*`，并需 `register.enabled` / `client.enabled`
- **Client 连接**：`url: http://localhost:18031` + `endpoint: /mcp`（满足验收「含 18031 与 /mcp」且避免路径重复）

## Deviations from Plan

### Auto-fixed Issues

**1. [Rule 3 - Blocking] 注解包路径对齐 1.1.2 真源**
- **Found during:** Task 1
- **Issue:** 教程/计划写 `org.springframework.ai.tool.annotation.McpTool`，1.1.2 JAR 中不存在
- **Fix:** 使用 `org.springaicommunity.mcp.annotation.McpTool` / `McpToolParam`
- **Files modified:** 31/33/34 全部 `@McpTool` 源码
- **Verification:** compile 通过；无 `org.springframework.ai.mcp.annotation` 导入
- **Committed in:** `61684a1` / `e91798f` / `cb03cb4`

**2. [Rule 3 - Blocking] ChatClient 挂载 MCP 工具 API**
- **Found during:** Task 2
- **Issue:** 1.1.2 `defaultTools` 不接受 `ToolCallbackProvider`
- **Fix:** 使用 `defaultToolCallbacks(new SyncMcpToolCallbackProvider(...))`
- **Files modified:** `examples/32-mcp-client-demo/.../McpClientController.java`
- **Verification:** compile 通过
- **Committed in:** `cd2baa2`

**3. [Rule 3 - Blocking] Nacos MCP Starter 坐标与配置键**
- **Found during:** Task 4
- **Issue:** 教程 `spring-ai-alibaba-starter-nacos-mcp-server/client` 在 1.1.2.2 不存在；Client 配置非 `service-names` 列表
- **Fix:** Server 用 `mcp-registry`，Client 用 `mcp-distributed`；`client.streamable.connections.*.service-name`；开启 `register.enabled` / `client.enabled`
- **Files modified:** 34 两模块 pom + application.yml
- **Verification:** 两模块 compile 通过
- **Committed in:** `cb03cb4`

**4. [Rule 2 - Missing Critical] 34 Client 显式挂载 ToolCallbackProvider**
- **Found during:** Task 4
- **Issue:** 教程 `chatClientBuilder.build()` 不会自动注入 Nacos 发现的工具
- **Fix:** `ObjectProvider<ToolCallbackProvider>` + `defaultToolCallbacks`
- **Files modified:** `AssistantController.java`
- **Verification:** compile 通过
- **Committed in:** `cb03cb4`

---

**Total deviations:** 4 auto-fixed（3 blocking API 对齐，1 missing critical）
**Impact on plan:** 行为与教程意图一致，仅适配 Spring AI 1.1.2 / SAA 1.1.2.2 真实 API；无范围蔓延。

## Issues Encountered

None beyond API surface alignment documented above.

## User Setup Required

- 32 真机 curl：先启动 31（`http://localhost:18031/mcp`）
- 34 真机：`bash scripts/infra.sh up cloud`（Nacos），开发凭证 `nacos`/`nacos`（生产须更换）
- Client 侧模型调用：`AI_DASHSCOPE_API_KEY`

## Next Phase Readiness

- MCP 能力域（31–34）compile 就绪，可供 Agent 章节作为远程工具源
- 未改 STATE.md / ROADMAP.md（按执行指令）

## Self-Check: PASSED

- FOUND: `examples/31-mcp-server-demo/pom.xml`
- FOUND: `examples/32-mcp-client-demo/pom.xml`
- FOUND: `examples/33-mcp-auth-demo/pom.xml`
- FOUND: `examples/34-mcp-nacos-demo/order-mcp-server/pom.xml`
- FOUND: `examples/34-mcp-nacos-demo/office-assistant-client/pom.xml`
- FOUND commits: `61684a1`, `cd2baa2`, `e91798f`, `cb03cb4`
- ALL five modules `mvn -q compile` exit 0

---
*Phase: 03-48-demo*
*Completed: 2026-07-04*
