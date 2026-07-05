# Phase 3 Archive: 48 个独立 Demo

**Status:** ✅ SHIPPED 2026-07-05  
**Milestone context:** v1.0（Phase 3 of 7）  
**Plans:** 14/14  
**UAT:** 48/48 pass

## Goal

学习者可对 examples/ 中全部 48 个 Demo 独立 `mvn spring-boot:run`，并用 curl 得到与章节一致的预期输出，且通过 HANDOFF §7 质量门禁。

## Success Criteria (verified)

1. ✅ 48 个 Demo 均存在独立工程，parent 指向仓库父 POM、子模块零版本号
2. ✅ 端口 `180NN`（Client = Server+100），密钥仅经环境变量注入
3. ✅ 每 Demo 具备 README、api.http、REST 入口与 curl 验证
4. ✅ 集成测试使用 `@EnabledIfEnvironmentVariable` + Testcontainers；复用 common/starter
5. ✅ compile gate + UAT 48/48 + 约定扫描（废弃 API/TODO/密钥/starter 边界）

## Plans Delivered

| Plan | Scope | Completed |
|------|-------|-----------|
| 03-01 | 首批基础 Demo 01~08 | 2026-07-04 |
| 03-02 | advisor/tool/memory 09~19 | 2026-07-04 |
| 03-03 | 首批 compile gate | 2026-07-04 |
| 03-04 | Structured Output 20~21 | 2026-07-04 |
| 03-05 | VectorStore 22~26 | 2026-07-04 |
| 03-06 | RAG 27~30 | 2026-07-04 |
| 03-07 | MCP 31~34 | 2026-07-04 |
| 03-08 | 次批 compile gate 20~34 | 2026-07-04 |
| 03-09 | Agent 35~37 | 2026-07-04 |
| 03-10 | Graph/Workflow 38~40 | 2026-07-04 |
| 03-11 | Multi-Agent 41~43 | 2026-07-04 |
| 03-12 | Stream+Observability+Logging 44~46 | 2026-07-04 |
| 03-13 | Routing+Fallback 47~48 | 2026-07-04 |
| 03-14 | Batch 3 compile gate 35~48 | 2026-07-04 |

## UAT Gap Closure (2026-07-05)

| Gap | Fix |
|-----|-----|
| 34 Nacos MCP Client 启动期工具未就绪 | lazy ChatClient + 排除 NacosMcpToolCallbackAutoConfiguration + nacos-init-dev.sh |
| 43 A2A 远程 Agent 返回空 | Server address/port/url + Client discovery-only + outputKey |
| 47/48 ModelRouter 注入失败 | starter @AutoConfigureAfter DashScope/DeepSeek |
| 18 Demo validation 静默失效 | 补 spring-boot-starter-validation |

## Key Decisions

- 17 用普通 Redis 自定义 ChatMemoryRepository（core profile）
- 25 必须 redis-stack-server（端口 6380）
- Embedding 统一 DashScope text-embedding-v4 dimensions=1024
- 自定义 Advisor 一律 CallAdvisor/StreamAdvisor
- Agent/Graph 用 spring-ai-alibaba-agent-framework
- Supervisor 用 ReactAgent + AgentTool.create
- A2A 用 spring.ai.alibaba.a2a.nacos.*
- 44~48 强制 saa-learning-starter

## Demo Inventory

01 quickstart · 02 autoconfig · 03 multi-model · 04 chat · 05 retry · 06 prompt · 07 prompt-builder · 08 prompt-nacos · 09 advisor · 10 custom-advisor · 11 tool · 12 dynamic-tool · 13 http-tool · 14 db-tool · 15 tool-security · 16 memory · 17 redis-memory · 18 jdbc-memory · 19 summary-memory · 20 structured-output · 21 json-schema · 22 embedding · 23 pgvector · 24 milvus · 25 redis-vector · 26 es-hybrid · 27 rag · 28 advanced-rag · 29 hybrid-rag · 30 rag-eval · 31 mcp-server · 32 mcp-client · 33 mcp-auth · 34 mcp-nacos · 35 agent · 36 agent-skills · 37 agent-hitl · 38 workflow · 39 graph-parallel · 40 graph-saga · 41 multi-agent · 42 supervisor · 43 a2a-nacos · 44 stream · 45 observability · 46 logging · 47 routing · 48 fallback

---
*Archived from ROADMAP.md Phase 3 section on 2026-07-05*
