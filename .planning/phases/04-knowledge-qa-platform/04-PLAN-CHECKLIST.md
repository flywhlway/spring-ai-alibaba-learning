# Phase 4 Plan Checklist — 知识库问答平台

**Phase:** 04-knowledge-qa-platform  
**Plans:** 6 plans · 6 waves (sequential)  
**Requirement:** REQ-phase-4-knowledge-qa  
**Created:** 2026-07-05

## Wave Structure

| Wave | Plan | Objective | depends_on | Autonomous |
|------|------|-----------|------------|------------|
| 1 | 04-01 | Wave 0 路径修复 + Entity/Repository/DTO 地基 | — | yes |
| 2 | 04-02 | config/* 基础设施（Security/MinIO/Memory/OpenAPI/Prompt 读取） | 04-01 | yes |
| 3 | 04-03 | rag/* + AiClientConfig + DemoKnowledgeSeeder | 04-02 | yes |
| 4 | 04-04 | 问答域 Auth/Qa/SSE/Conversation/Feedback | 04-03 | yes |
| 5 | 04-05 | admin/* 五组后台 + Prompt 发布 + @Tool | 04-04 | yes |
| 6 | 04-06 | 测试 + HANDOFF §7 + 04-UAT.md | 04-05 | yes |

## Plan Files

- [ ] **04-01-PLAN.md** — 畸形包路径迁移；KqaProperties；8 Entity；Repository/Mapper/DTO/VO
- [ ] **04-02-PLAN.md** — SecurityConfig JWT；MinIO/VectorStore/ChatMemory/Async/OpenAPI；PromptTemplateProvider
- [ ] **04-03-PLAN.md** — RagPipelineFactory；DocumentEtlPipeline；CitationPostProcessor；AiClientConfig；Seeder
- [ ] **04-04-PLAN.md** — Auth/Qa 同步+SSE/Conversation/Feedback Controller+Service
- [ ] **04-05-PLAN.md** — Document/Prompt/User/Audit/Dashboard admin；PromptPublishService；KnowledgeOpsTools
- [ ] **04-06-PLAN.md** — 单测+Testcontainers IT；clean install；version-audit；04-UAT.md

## Decision Coverage (CONTEXT D-01 ~ D-31)

| Decision | Plan(s) |
|----------|---------|
| D-01~D-08 棕地/约定 | 04-01, 04-02, 全 plan compile 门禁 |
| D-09~D-12 RAG/Milvus | 04-03 |
| D-13~D-15 Citation/SSE | 04-04 |
| D-16~D-18 ETL | 04-03, 04-05 |
| D-19~D-22 ChatClient/Prompt | 04-02, 04-03, 04-05 |
| D-23~D-25 Security/Tool | 04-02, 04-05 |
| D-26~D-27 Admin/看板 | 04-05 |
| D-28~D-31 测试/验收 | 04-06 |

## Multi-Source Coverage Audit

| Source | Item | Plan |
|--------|------|------|
| GOAL | 上传/解析知识 + Citation 问答 | 04-03, 04-04, 04-05 |
| GOAL | SSE + 多模型 + Redis Memory | 04-02, 04-03, 04-04 |
| GOAL | 统一交付标准 + 19100 | 04-06 |
| GOAL | PG+Milvus+Redis+Security+审计 | 04-01~05 |
| REQ | REQ-phase-4-knowledge-qa | 全 6 plan |
| RESEARCH | Wave 0 路径修复 | 04-01 |
| RESEARCH | DemoKnowledgeSeeder | 04-03 |
| RESEARCH | testcontainers-redis pom | 04-06 |
| CONTEXT | 五波 README §9 顺序 | 04-01~06 |
| CONTEXT | HANDOFF §7 + 04-UAT.md | 04-06 |

**Excluded (deferred):** 独立前端、文档 ACL、Rerank 服务、语义缓存、ES 混合检索

## Per-Wave Compile Gate

每 plan 完成前执行：

```bash
mvn -f projects/knowledge-qa-platform/pom.xml -q compile
```

末波（04-06）全量：

```bash
mvn -f projects/knowledge-qa-platform/pom.xml clean install
bash scripts/version-audit.sh
bash scripts/spring-ai-2-readiness.sh .
```

## Execute

```bash
/gsd-execute-phase 04-knowledge-qa-platform
```

<sub>`/clear` 建议每 2~3 plan 刷新上下文</sub>
