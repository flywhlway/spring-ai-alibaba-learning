# 42-supervisor-demo

`ReactAgent` 总控 + `AgentTool.create(subAgent)` 办公助手（对应教程第 15 章 §15.7，**无** `SupervisorAgent` 类）。

## 前置条件
- 中间件：无
- 环境变量：`AI_DASHSCOPE_API_KEY`

## 运行
```bash
mvn spring-boot:run    # 端口 18042
```

## 接口
| 方法 | 路径 | 说明 |
|---|---|---|
| GET | `/supervisor/chat?query=` | 总控调度 calendar-agent / email-agent |

## 快速验证
```bash
curl "http://localhost:18042/supervisor/chat?query=帮我查一下2026-07-05的日程"
curl "http://localhost:18042/supervisor/chat?query=给张经理起草一封项目进度汇报邮件，要点：Phase3进行中、下周联调"
```

预期：`{"code":0,"message":"success","data":"..."}` 且 `data` 非空。

## 源码导读
| 类 | 职责 | 教程小节 |
|---|---|---|
| `CalendarTools` / `EmailTools` | 子 Agent 的 `@Tool` | §15.7 |
| `OfficeSupervisorConfig` | 子 Agent `.description()` + `AgentTool.create` | §15.7 |
| `SupervisorController` | `supervisor.call(query).getText()` | §15 |

## API 纠偏说明
- 使用 `com.alibaba.cloud.ai.graph.agent.AgentTool.create(reactAgent)`
- **禁止** `SupervisorAgent`、`AgentTool.from(agent, desc)`
