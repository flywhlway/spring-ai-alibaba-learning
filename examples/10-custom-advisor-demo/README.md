# 10-custom-advisor-demo

自定义 `AuditLoggingAdvisor`（`CallAdvisor` + `StreamAdvisor`）：审计日志手机号脱敏，
不影响送往模型的原文（对应教程第 06 章 §6.6）。

## 前置条件
- 中间件：无
- 环境变量：`AI_DASHSCOPE_API_KEY`

## 运行
```bash
mvn spring-boot:run    # 端口 18010
```

## 接口
| 方法 | 路径 | 说明 |
|---|---|---|
| GET | `/ask?question=` | 经审计 Advisor 链调用模型 |

## 快速验证
```bash
curl "http://localhost:18010/ask?question=我的手机号是13812345678，麻烦查一下我的订单状态"
```
预期日志（节选）：
```text
[audit] request user_text=我的手机号是138****5678，麻烦查一下我的订单状态
DEBUG ... SimpleLoggerAdvisor : request: ...原始未脱敏文本...
[audit] response cost=...ms
```

## 源码导读
| 类 | 职责 | 教程小节 |
|---|---|---|
| `AuditLoggingAdvisor` | 脱敏审计 + 耗时 | §6.6 |
| `AdvisorDemoController` | 注册自定义 Advisor + SimpleLogger | 可运行 Demo |

## 运行结果
截图存放于 `images/examples/10-custom-advisor-demo/`（真机运行后补充）。
