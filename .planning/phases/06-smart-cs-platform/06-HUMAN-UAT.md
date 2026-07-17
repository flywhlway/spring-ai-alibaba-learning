---
status: diagnosed
phase: 06-smart-cs-platform
source:
  - 06-VERIFICATION.md
  - projects/smart-cs-platform/scripts/uat-smart-cs.sh
  - .planning/phases/06-smart-cs-platform/uat-logs/scs-uat.log
started: 2026-07-17T14:45:00Z
updated: 2026-07-17T16:00:00Z
scope: automated curl UAT (--auto)
---

## Current Test

[testing complete]

## Tests

### 1. 健康检查与三角色登录
expected: health UP；admin/agent1/customer1 均 code=0 且拿到 accessToken；角色 claim 正确
result: issue
reported: "应用冷启动失败：先因 scs-db-init 未挂上 db/（model_profile 表不存在），手工灌库后仍因 RewriteQueryTransformer 要求 {target} 占位符而 Bean 创建失败，19300 从未 UP"
severity: blocker

### 2. FAQ ask + SSE stream
expected: ask 返回答案；stream 含 message/done（FAQ 路径可出现 cacheHit）；cs_message 持久化
result: blocked
blocked_by: server
reason: "应用未启动，无法执行 ask/stream"

### 3. 工单流转 + HITL handoff start/approve
expected: 非法 transition 400；approve 后工单 HUMAN_HANDLING，HITL resume 成功
result: blocked
blocked_by: server
reason: "应用未启动；另 STATE Pending 已记 06-REVIEW Critical（HITL pending 未注册）"

### 4. 运营看板 + Nacos + 监控
expected: stats 含会话/工单/cacheHitRate/成本字段；Nacos 出现 scs.model.profiles 与 prompt Data ID；prometheus 可 scrape
result: blocked
blocked_by: server
reason: "应用未启动，无法调 dashboard/stats 与 scrape"

### 5. uat-smart-cs.sh smoke
expected: 脚本 exit 0；与 06-UAT.md 预期一致
result: issue
reported: "两次运行均 exit 1：应用启动超时。日志 scs-uat.log 先后报 model_profile 不存在、以及 placeholders must be present: target"
severity: blocker

## Summary

total: 5
passed: 0
issues: 2
pending: 0
skipped: 0
blocked: 3

## UAT Run

| Run | Date | Pass | Fail | Notes |
|-----|------|------|------|-------|
| v1 --auto | 2026-07-17 23:44~23:47 | 0 | startup | scs-db-init 挂载到 docker/db（空目录），schema 未导入 |
| v1+manual-db | 2026-07-17 23:48~23:51 | 0 | startup | 手工灌库后 RewriteQueryTransformer 缺 {target} |

**验收命令：** `bash projects/smart-cs-platform/scripts/uat-smart-cs.sh`（需 Key + smartcs compose profiles）

**证据：**
- `docker inspect saa-scs-db-init` Mounts.Source = `.../docker/db`（应为 `projects/smart-cs-platform/db`）
- `scs-uat.log`: `relation "public.model_profile" does not exist` → 随后 `placeholders must be present in the prompt template: target`

## Gaps

- truth: "按文档 compose + smartcs profile 后，scs_platform 自动拥有 schema/data，应用可冷启动 health UP"
  status: failed
  reason: "User reported: scs-db-init volume ./db 相对首个 compose 文件解析到 docker/db，schema.sql 缺失，init exit 1"
  severity: blocker
  test: 1
  root_cause: "多文件 compose 时 override 内 ./db 相对首个文件父目录 docker/ 解析，挂载到空的 docker/db；kqa/office/smartcs 三处 override 同模式"
  artifacts:
    - path: "projects/smart-cs-platform/docker-compose.override.yml"
      issue: "volumes ./db:/scs-db:ro 解析错误"
    - path: "projects/knowledge-qa-platform/docker-compose.override.yml"
      issue: "同样 ./db 相对路径风险"
    - path: "projects/office-agent-assistant/docker-compose.override.yml"
      issue: "同样 ./db 相对路径风险"
  missing:
    - "将 volume 改为 ../projects/<proj>/db（相对 docker/）或统一 --project-directory"
    - "smartcs monitor 的 ./monitor/prometheus.yml 同步修正"
  debug_session: ".planning/debug/scs-db-init-volume.md"

- truth: "query-rewrite Prompt 满足 Spring AI RewriteQueryTransformer 必填占位符 {target}+{query}，RetrievalAugmentationAdvisor Bean 可创建"
  status: failed
  reason: "User reported: 应用启动失败 IllegalArgumentException: placeholders must be present: target；DB/classpath 模板仅有 {query}"
  severity: blocker
  test: 5
  root_cause: "Spring AI 1.1.2 RewriteQueryTransformer 强制 {target}+{query}；classpath 与 db/data.sql 种子均缺 {target}；DB PUBLISHED 优先于 classpath"
  artifacts:
    - path: "projects/smart-cs-platform/src/main/resources/prompts/query-rewrite.st"
      issue: "仅含 {query}"
    - path: "projects/smart-cs-platform/db/data.sql"
      issue: "query-rewrite 种子仅含 {query}"
    - path: "projects/smart-cs-platform/src/main/java/com/flywhl/saa/smartcs/rag/RagPipelineFactory.java"
      issue: "注入自定义 promptTemplate 触发 PromptAssert"
  missing:
    - "模板补齐 {target}+{query}（classpath + data.sql + test 副本）"
    - "已有库需重跑 init 或幂等 UPDATE 已发布 query-rewrite"
  debug_session: ".planning/debug/query-rewrite-target.md"
