# 05-retry-demo

`spring.ai.retry.*` 重试机制、自定义 `RetryTemplate` 与错误处理策略演示（对应教程第 04 章 §4.8）。

## 前置条件
- 中间件：无
- 环境变量：`AI_DASHSCOPE_API_KEY`

## 运行
```bash
mvn spring-boot:run    # 端口 18005
```

## 接口
| 方法 | 路径 | 说明 |
|---|---|---|
| GET | `/chat?message=` | 正常问答（瞬态错误走自定义 RetryTemplate） |
| GET | `/retry/policy` | 当前 `spring.ai.retry.*` 绑定摘要 |
| GET | `/retry/stats` | 自定义 RetryListener 累计 open/error/success |

## 快速验证
```bash
curl "http://localhost:18005/retry/policy"
curl "http://localhost:18005/chat?message=你好"
curl "http://localhost:18005/retry/stats"
```
预期 `/retry/policy`（节选）：
```json
{"code":0,"message":"success","data":{"maxAttempts":3,"initialInterval":1000,"multiplier":2,"maxInterval":10000}}
```

## 源码导读
| 类 | 职责 | 教程小节 |
|---|---|---|
| `RetryConfig` | 自定义 `RetryTemplate` Bean，覆盖官方 `@ConditionalOnMissingBean` | §4.8 |
| `RetryAttemptCounter` | `RetryListener` 统计重试上下文 | §4.8 |
| `RetryController` | 问答 + 策略/统计可读接口 | §4.8 |

## 运行结果
截图存放于 `images/examples/05-retry-demo/`（真机运行后补充）。
