# 44-stream-demo

统一 SSE 事件协议（`message` / `error` / `done`）流式问答 + starter `AuditLoggingAdvisor`（对应教程第 17 章）。

## 前置条件
- 中间件：无
- 环境变量：`AI_DASHSCOPE_API_KEY`

## 运行
```bash
mvn spring-boot:run    # 端口 18044
```

## 接口
| 方法 | 路径 | 说明 |
|---|---|---|
| GET | `/chat/stream-unified?message=` | SSE 流式问答，事件类型见下表 |

### SSE 事件协议
| event | data | 说明 |
|---|---|---|
| `message` | 文本增量 | 模型输出片段 |
| `error` | `Result` JSON | 业务错误码，不含堆栈 |
| `done` | 空字符串 | 流结束标记 |

## 快速验证
```bash
curl -N "http://localhost:18044/chat/stream-unified?message=用一句话介绍Spring%20AI"
```
预期：连续 `event:message` 片段，最后 `event:done`。

## 源码导读
| 类 | 职责 | 教程小节 |
|---|---|---|
| `StreamConfig` | `defaultAdvisors(AuditLoggingAdvisor)` | §17 + §19 starter |
| `StreamController` | 统一 SSE 协议 + `Result.fail` 错误事件 | §17.5 |

## 冒烟 IT
```bash
# 需 AI_DASHSCOPE_API_KEY
mvn -f examples/44-stream-demo/pom.xml test
```

## 运行结果
截图存放于 `images/examples/44-stream-demo/`（真机运行后补充）。
