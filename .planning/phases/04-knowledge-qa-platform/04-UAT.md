---
status: complete
phase: 04-knowledge-qa-platform
result: all_pass
closed: 2026-07-18
evidence: 04-HUMAN-UAT.md + scripts/uat-knowledge-qa.sh 8/0
---

# Phase 04 · knowledge-qa-platform UAT 验收清单

> 端口 **19100**；演示账号 `admin/admin123`（ADMIN）、`zhangsan/zhangsan123`（EMPLOYEE）。
> 自动化脚本：`bash scripts/uat-knowledge-qa.sh`（无 Key 仅 health+login；有 Key 全量）。

## 前置条件

```bash
# 1. 环境变量（DashScope Key 可选，无 Key 时跳过 ask/stream）
source scripts/setup-env.sh

# 2. 中间件（Milvus 冷启动 30~60s）
docker compose -f docker/docker-compose.yml \
  -f projects/knowledge-qa-platform/docker-compose.override.yml \
  --profile core --profile vector --profile cloud --profile kqa up -d

# 3. 编译
mvn -pl common,starter -am clean install -DskipTests
mvn -f projects/knowledge-qa-platform/pom.xml spring-boot:run
```

---

## 0. 健康检查（无需鉴权）

```bash
curl -s http://localhost:19100/actuator/health | jq .
```

**预期：** `status` 为 `UP`。

---

## 1. 认证域

### 1.1 管理员登录

```bash
curl -s -X POST http://localhost:19100/api/auth/login \
  -H 'Content-Type: application/json' \
  -d '{"username":"admin","password":"admin123"}' | jq .
```

**预期：** `code=0`，`data.accessToken` 非空，`data.user.role=ADMIN`。

### 1.2 员工登录

```bash
curl -s -X POST http://localhost:19100/api/auth/login \
  -H 'Content-Type: application/json' \
  -d '{"username":"zhangsan","password":"zhangsan123"}' | jq .
```

**预期：** `code=0`，`data.user.role=EMPLOYEE`。

### 1.3 当前用户（需 Bearer）

```bash
export USER_TOKEN="<zhangsan accessToken>"
curl -s http://localhost:19100/api/auth/me \
  -H "Authorization: Bearer ${USER_TOKEN}" | jq .
```

**预期：** `code=0`，`data.username=zhangsan`。

---

## 2. 问答域（员工，需 `AI_DASHSCOPE_API_KEY`）

### 2.1 同步问答（含 citations 闭环）

```bash
curl -s -X POST http://localhost:19100/api/qa/ask \
  -H "Authorization: Bearer ${USER_TOKEN}" \
  -H 'Content-Type: application/json' \
  -d '{"conversationId":"demo-conv-001","question":"员工出差住宿费报销标准是多少？"}' | jq .
```

**预期：** `code=0`，`data.answer` 含「600」或「住宿费」，`data.citations` 数组非空。

### 2.2 SSE 流式问答

```bash
curl -sN -H "Authorization: Bearer ${USER_TOKEN}" \
  -H 'Accept: text/event-stream' \
  'http://localhost:19100/api/qa/stream?conversationId=demo-conv-001&question=智能网关如何恢复出厂设置'
```

**预期：** 出现 `event:message` 增量、`event:meta`（引用/usage）、`event:done`。

### 2.3 会话列表

```bash
curl -s 'http://localhost:19100/api/conversations?page=1&size=10' \
  -H "Authorization: Bearer ${USER_TOKEN}" | jq .
```

**预期：** `code=0`，`data.records` 为数组。

### 2.4 会话历史

```bash
curl -s 'http://localhost:19100/api/conversations/demo-conv-001/messages?page=1&size=20' \
  -H "Authorization: Bearer ${USER_TOKEN}" | jq .
```

**预期：** `code=0`，含 user/assistant 消息。

### 2.5 删除会话

```bash
curl -s -X DELETE http://localhost:19100/api/conversations/demo-conv-001 \
  -H "Authorization: Bearer ${USER_TOKEN}" | jq .
```

**预期：** `code=0`。

### 2.6 答案反馈

```bash
curl -s -X POST http://localhost:19100/api/qa/feedback \
  -H "Authorization: Bearer ${USER_TOKEN}" \
  -H 'Content-Type: application/json' \
  -d '{"messageId":1,"rating":1,"comment":"答案准确"}' | jq .
```

**预期：** `code=0`（`messageId` 需为有效 ID）。

### 2.7 RBAC：员工访问后台 403

```bash
curl -s -o /dev/null -w '%{http_code}\n' \
  -H "Authorization: Bearer ${USER_TOKEN}" \
  'http://localhost:19100/api/admin/users?page=1&size=10'
```

**预期：** HTTP `403`。

---

## 3. 后台-知识管理（ADMIN）

```bash
export ADMIN_TOKEN="<admin accessToken>"
```

### 3.1 文档列表

```bash
curl -s 'http://localhost:19100/api/admin/documents?status=INDEXED&page=1&size=10' \
  -H "Authorization: Bearer ${ADMIN_TOKEN}" | jq .
```

**预期：** `code=0`，含演示文档「差旅费报销制度」。

### 3.2 触发重建索引

```bash
curl -s -X POST http://localhost:19100/api/admin/documents/1/reindex \
  -H "Authorization: Bearer ${ADMIN_TOKEN}" | jq .
```

**预期：** `code=0`。

### 3.3 上传文档（multipart，需样例 PDF）

```bash
curl -s -X POST http://localhost:19100/api/admin/documents \
  -H "Authorization: Bearer ${ADMIN_TOKEN}" \
  -F 'title=测试文档' -F 'category=公司制度' \
  -F 'file=@projects/knowledge-qa-platform/http/samples/travel-expense-policy-2026.pdf'
```

**预期：** `code=0`，返回 `documentId`。

### 3.4 删除文档

```bash
curl -s -X DELETE http://localhost:19100/api/admin/documents/3 \
  -H "Authorization: Bearer ${ADMIN_TOKEN}" | jq .
```

**预期：** `code=0`（ID 存在时）。

---

## 4. 后台-Prompt 管理（ADMIN）

### 4.1 模板列表

```bash
curl -s 'http://localhost:19100/api/admin/prompts?templateKey=qa-system' \
  -H "Authorization: Bearer ${ADMIN_TOKEN}" | jq .
```

**预期：** `code=0`，含 `PUBLISHED` 版本。

### 4.2 新建草稿

```bash
curl -s -X POST http://localhost:19100/api/admin/prompts \
  -H "Authorization: Bearer ${ADMIN_TOKEN}" \
  -H 'Content-Type: application/json' \
  -d '{"templateKey":"qa-system","content":"你是助手…\n{context}","description":"V3 草稿"}' | jq .
```

**预期：** `code=0`，`status=DRAFT`。

### 4.3 发布版本

```bash
curl -s -X POST http://localhost:19100/api/admin/prompts/3/publish \
  -H "Authorization: Bearer ${ADMIN_TOKEN}" | jq .
```

**预期：** `code=0`（需 Nacos 8848 可用）。

---

## 5. 后台-用户 / 审计 / 看板（ADMIN）

### 5.1 用户列表

```bash
curl -s 'http://localhost:19100/api/admin/users?page=1&size=10' \
  -H "Authorization: Bearer ${ADMIN_TOKEN}" | jq .
```

### 5.2 新建用户

```bash
curl -s -X POST http://localhost:19100/api/admin/users \
  -H "Authorization: Bearer ${ADMIN_TOKEN}" \
  -H 'Content-Type: application/json' \
  -d '{"username":"wangwu","password":"wangwu123","displayName":"王五","role":"EMPLOYEE","department":"财务部"}' | jq .
```

### 5.3 停用用户

```bash
curl -s -X PUT 'http://localhost:19100/api/admin/users/3/status?enabled=false' \
  -H "Authorization: Bearer ${ADMIN_TOKEN}" | jq .
```

### 5.4 审计日志

```bash
curl -s 'http://localhost:19100/api/admin/audits?action=LOGIN&page=1&size=20' \
  -H "Authorization: Bearer ${ADMIN_TOKEN}" | jq .
```

### 5.5 运营看板

```bash
curl -s 'http://localhost:19100/api/admin/dashboard/stats?days=7' \
  -H "Authorization: Bearer ${ADMIN_TOKEN}" | jq .
```

**预期（5.x）：** 均为 `code=0`。

---

## 6. 可观测

### 6.1 Prometheus

```bash
curl -s http://localhost:19100/actuator/prometheus | head -20
```

**预期：** 含 `gen_ai_client_token_usage` 等指标。

### 6.2 API 文档

浏览器打开：`http://localhost:19100/doc.html`

---

## HANDOFF §7 质量门禁

| 检查项 | 命令 | 预期 |
|--------|------|------|
| 编译 | `mvn -f projects/knowledge-qa-platform/pom.xml clean install` | 无 Key 全绿 |
| 版本审计 | `bash scripts/version-audit.sh` | 全绿 |
| 2.0 破坏点 | `bash scripts/spring-ai-2-readiness.sh .` | 低位 |
| 废弃 API | `rg 'PromptChatMemoryAdvisor\|FunctionCallback\|CallAroundAdvisor' projects/knowledge-qa-platform` | 无匹配 |
| 硬编码密钥 | `rg 'sk-[a-zA-Z0-9]{20,}' projects/knowledge-qa-platform` | 无匹配 |
| UAT 脚本 | `bash scripts/uat-knowledge-qa.sh` | 有 infra 时全绿 |

---

*对应 api.http：`projects/knowledge-qa-platform/http/api.http`*

---

## 自动化验收结果（2026-07-18）

| 项 | 结果 |
|----|------|
| `bash scripts/uat-knowledge-qa.sh` | **8 通过 / 0 失败**，exit 0 |
| HUMAN-UAT 记录 | [`04-HUMAN-UAT.md`](./04-HUMAN-UAT.md) |
