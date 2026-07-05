# Phase 4: 知识库问答平台 - Discussion Log

> **Audit trail only.** Do not use as input to planning, research, or execution agents.
> Decisions are captured in CONTEXT.md — this log preserves the alternatives considered.

**Date:** 2026-07-05
**Phase:** 4-知识库问答平台
**Mode:** `--auto`
**Areas discussed:** 交付波次, RAG 管线, Citation/SSE 协议, 文档入库, 安全认证, 测试验收

---

## 交付波次与棕地策略

| Option | Description | Selected |
|--------|-------------|----------|
| 垂直切片（按用例端到端） | 每个用户故事贯穿全栈 | |
| 水平分层（README §9 五波） | config → rag → qa → admin → test | ✓ |
| 重写骨架契约 | 调整 API/DDL 后再实现 | |

**Auto choice:** 水平分层五波（D-01~D-04），对齐 README §9，每波 compile 门禁，末波全量 UAT。
**Notes:** 用户要求基于已完成骨架实现全部占位，不重设计契约。

---

## RAG 管线策略

| Option | Description | Selected |
|--------|-------------|----------|
| Naive RAG（QuestionAnswerAdvisor） | 简单向量检索 | |
| Modular RAG（28-demo 模式） | RewriteQuery + Retriever + PostProcessor + 空上下文拒答 | ✓ |
| 混合 ES + Milvus | 双路检索 | |

**Auto choice:** Modular RAG，参数绑定 `kqa.rag.*`，embedding v3 1024 维。
**Notes:** Phase 3 STATE 记录 demo 用 v4，但本项目 application.yml 已锁 v3，保持骨架一致。

---

## Citation 与 SSE 协议

| Option | Description | Selected |
|--------|-------------|----------|
| 脚注内联 [1][2] | 答案正文嵌入引用标号 | |
| 独立 citations + meta 事件 | 同步 body / SSE meta 承载引用数组 | ✓ |

**Auto choice:** README §6 协议——`message`/`meta`/`done`/`error` 事件类型不变。

---

## 文档入库流程

| Option | Description | Selected |
|--------|-------------|----------|
| 同步解析 | 上传阻塞至 INDEXED | |
| 异步 ETL | 上传立即返回，后台 PARSING→INDEXED/FAILED | ✓ |

**Auto choice:** @Async + 状态机 + IngestStatusTracker。

---

## 安全认证

| Option | Description | Selected |
|--------|-------------|----------|
| Session Cookie | 传统会话 | |
| JWT Resource Server | login 签发 + Bearer 校验 | ✓ |
| 外部 IdP OIDC | 企业 SSO | |

**Auto choice:** 自签发 JWT；演示 `{noop}` + 新建 BCrypt。

---

## 测试与验收

| Option | Description | Selected |
|--------|-------------|----------|
| 仅编译通过 | 最小验收 | |
| HANDOFF §7 + curl UAT | 全门禁 + api.http 闭环 | ✓ |

**Auto choice:** Testcontainers(PG+Redis) + API Key 门控 IT + 04-UAT.md。

---

## Claude's Discretion

- SSE 实现技术选型（SseEmitter vs Flux）
- Repository 派生查询命名
- 单测细粒度分配

## Deferred Ideas

- 独立管理前端、文档级 ACL、专用 Rerank 服务、语义缓存、ES 混合检索
