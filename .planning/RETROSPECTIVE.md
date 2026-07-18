# Retrospective: spring-ai-alibaba-learning

## Milestone: v1.0 — Full Delivery

**Shipped:** 2026-07-18  
**Phases:** 7 | **Plans:** ~36

### What Was Built

脚手架与教程基座、48 个可运行 Demo、三个企业项目（知识库 / 办公 Agent / 智能客服）、统一 quality-gate 与 CI 骨架。

### What Worked

- 版本锁定（Boot 3.5.16 / SAA 1.1.2.2）减少漂移
- Demo 端口与 examples/README SSOT 降低冲突
- 脚本 UAT（uat-*.sh）可替代大量人工验收
- Gap closure plans（06-08/09/10）快速关闭冷启动 blocker
- Code review → REVIEW-FIX 闭环 HITL Critical

### What Was Inefficient

- Security 6.5 JWT 默认 RS256 在三项目重复踩坑（后统一 HS256）
- RewriteQueryTransformer `{target}` 在 kqa/scs 重复出现
- office `vectorDataSource` 抢占主 DataSource（多数据源缺 @Primary）
- MCP Client 自连本机冷启动鸡生蛋
- Phase 5 brownfield 缺 VERIFICATION/SUMMARY 导致审计可见性差

### Patterns Established

- 双 ChatModel 场景显式提供 `ChatClient.Builder`
- JWT HMAC 必须 `JwsHeader.with(MacAlgorithm.HS256)`
- 企业项目 compose override volume 相对 `docker/` → `../projects/...`
- UAT 脚本自启应用 + Key 门控用例分层

### Key Lessons

1. 多 DataSource 必须显式 `@Primary`，勿依赖 Boot 单 DS 假设
2. Spring AI RAG 占位符约束要以运行时 API 为准，种子 SQL 需幂等 UPDATE
3. 里程碑关闭前先跑 `audit-open` 清 debug/UAT/verification 状态
4. HUMAN-UAT 脚本化比纯文档清单更可复现

## Cross-Milestone Trends

（首个完整里程碑；待 v1.1+ 追加对比）
