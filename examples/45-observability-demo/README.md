# 45-observability-demo

Micrometer + Prometheus 指标导出 + starter `CostTrackingObservationHandler` 成本采集（对应教程第 18 章）。

## 前置条件
- 中间件：无（Grafana 看板仅文档说明，本 Demo 不内嵌 compose）
- 环境变量：`AI_DASHSCOPE_API_KEY`

## 运行
```bash
mvn spring-boot:run    # 端口 18045
```

## 接口
| 方法 | 路径 | 说明 |
|---|---|---|
| GET | `/obs/chat?message=` | 触发模型调用，产生 gen_ai.* 指标与成本日志 |
| GET | `/actuator/prometheus` | Prometheus 文本格式指标（**生产须鉴权/网络隔离**） |
| GET | `/actuator/health` | 健康检查 |

## 快速验证
```bash
# 1. 触发一次模型调用
curl "http://localhost:18045/obs/chat?message=hello"

# 2. 查看 Prometheus 指标（含 gen_ai_client_operation 等）
curl "http://localhost:18045/actuator/prometheus" | grep gen_ai
```

## Grafana 看板（可选）
1. 启动 Prometheus 抓取 `localhost:18045/actuator/prometheus`
2. 导入 Spring Boot / Micrometer 社区看板，关注 `gen_ai.usage.*` 与 ChatClient 耗时
3. 成本明细见应用日志（starter `LoggingCostRecorder`）

## 源码导读
| 类 | 职责 | 教程小节 |
|---|---|---|
| `ObservabilityConfig` | `defaultAdvisors(AuditLoggingAdvisor)` | §18 + §19 |
| starter 自动装配 | `CostTrackingObservationHandler` + `LoggingCostRecorder` | §18.4 |

## 运行结果
截图存放于 `images/examples/45-observability-demo/`（真机运行后补充）。
