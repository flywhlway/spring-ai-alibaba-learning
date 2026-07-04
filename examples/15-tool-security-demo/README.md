# 15-tool-security-demo

Tool 权限控制与调用审计演示：变更类工具（`deleteDocument`）强制通过 `ToolContext`
校验调用方角色，非管理员被拒绝；所有工具调用（无论成败）都写入一条审计日志
（角色、工具名、参数摘要、结果状态、耗时）。身份信息绝不作为普通工具参数暴露给模型
（否则等于允许用户话术伪造身份），完全由服务端通过 `toolContext(...)` 注入
（对应教程第 07 章 §7.4 / §7.5 与可运行 Demo 小节）。

## 前置条件
- 中间件：无
- 环境变量：`AI_DASHSCOPE_API_KEY`

## 运行
```bash
mvn spring-boot:run    # 端口 18015
```

## 接口
| 方法 | 路径 | 说明 |
|---|---|---|
| GET | `/admin/ask?question=&role=` | 模型驱动调用知识库管理工具，`role` 默认 `USER` |

## 快速验证
```bash
# 普通用户尝试删除文档 —— 应被拒绝
curl "http://localhost:18015/admin/ask?question=删除ID为doc-001的文档&role=USER"

# 管理员执行同样操作 —— 应成功
curl "http://localhost:18015/admin/ask?question=删除ID为doc-001的文档&role=ADMIN"
```
预期输出：
```text
权限不足：删除操作仅管理员可执行，当前角色为 USER
文档 doc-001 已删除
```
应用日志（`AUDIT` logger）会同步打印每次调用的角色/工具名/参数/结果/耗时。

## 源码导读
| 类 | 职责 | 教程小节 |
|---|---|---|
| `KnowledgeAdminTools` | `ToolContext` 角色校验 + `returnDirect` 短路返回 + 审计日志 | §7.4 / §7.5 |
| `ToolSecurityController` | `toolContext(Map.of("role", role))` 注入身份 | §7.4 |

## 运行结果
截图存放于 `images/examples/15-tool-security-demo/`（真机运行后补充）。
