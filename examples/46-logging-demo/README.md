# 46-logging-demo

MDC `traceId` + starter `AuditLoggingAdvisor` 结构化日志贯通（对应教程第 18 章 Logging 段落）。

## 前置条件
- 中间件：无
- 环境变量：`AI_DASHSCOPE_API_KEY`

## 运行
```bash
mvn spring-boot:run    # 端口 18046
```

## 接口
| 方法 | 路径 | 说明 |
|---|---|---|
| GET | `/log/chat?message=` | 同步问答，触发业务日志与 AUDIT 审计日志 |

## 快速验证
```bash
curl "http://localhost:18046/log/chat?message=你好"
```

观察控制台日志，同一请求的业务日志与 `AUDIT` logger 输出应携带**相同** `[traceId]`：

```
2026-07-04 12:00:00.123 [http-nio-18046-exec-1] [a1b2c3d4-...] INFO  c.f.s.l.LoggingController - 收到聊天请求 messageLength=2
2026-07-04 12:00:01.456 [http-nio-18046-exec-1] [a1b2c3d4-...] INFO  AUDIT - [audit] request user_text=你好
2026-07-04 12:00:02.789 [http-nio-18046-exec-1] [a1b2c3d4-...] INFO  AUDIT - [audit] response cost=1234ms
2026-07-04 12:00:02.790 [http-nio-18046-exec-1] [a1b2c3d4-...] INFO  c.f.s.l.LoggingController - 聊天完成 contentLength=42
```

`Result` JSON 响应中的 `traceId` 字段也会与日志一致（common 模块从 MDC 读取）。

## 源码导读
| 类 | 职责 | 教程小节 |
|---|---|---|
| `TraceIdFilter` | 请求入口写入 MDC `traceId` | §18 Logging |
| `LoggingConfig` | `defaultAdvisors(AuditLoggingAdvisor)` | §18 + §19 |
| `application.yml` | `logging.pattern.console` 含 `%X{traceId}` | §18 |

## 运行结果
截图存放于 `images/examples/46-logging-demo/`（真机运行后补充）。
