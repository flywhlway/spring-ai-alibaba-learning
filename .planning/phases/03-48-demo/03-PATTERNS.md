# Phase 3 Batch 2 — Pattern Map

**Mapped:** 2026-07-04  
**Scope:** demos 20~34（新建）

## PATTERN MAPPING COMPLETE

| New path | Role | Closest analog | Reuse |
|----------|------|----------------|-------|
| `examples/2N-*/pom.xml` | Maven module | `examples/04-chat-demo/pom.xml` | parent + dashscope + common，零版本号 |
| `*Application.java` | Boot entry | `examples/04-chat-demo/.../ChatDemoApplication.java` | `@SpringBootApplication` + `@Import(GlobalExceptionHandler.class)` |
| `application.yml` | Config | `examples/11-tool-demo/.../application.yml` | `server.port` + `${AI_DASHSCOPE_API_KEY}` |
| `*Controller.java` | REST | `examples/16-memory-demo/.../MemoryController.java` | `Result.ok(...)` |
| VectorStore demos | Store CRUD | 教程 11 Controller 片段 | `VectorStore.add` / `similaritySearch` |
| RAG demos | Advisor chain | 教程 09 RagController | `QuestionAnswerAdvisor` / `RetrievalAugmentationAdvisor` |
| MCP tools | Tool exposure | `examples/11-tool-demo/.../MemberTools.java` | 注解风格对齐，换 `@McpTool` |
| README + api.http | Docs | `examples/README.md` §3 模板 | 前置条件写 infra profile |

## Do not copy

- `PromptChatMemoryAdvisor` / `CallAroundAdvisor` / `FunctionCallback`
- 硬编码密钥
- 普通 `redis:7.4-alpine` 用于 25（必须 Stack）
- Spring AI 2.0 MCP 包名
