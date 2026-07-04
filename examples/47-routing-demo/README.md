# 47-routing-demo

starter `ModelRouter` 多模型智能路由（对应教程第 20 章 §20.2 路由段落）。

## 前置条件
- 中间件：无
- 环境变量：
  - `AI_DASHSCOPE_API_KEY`（必需）
  - `DEEPSEEK_API_KEY`（可选；完整主备双模型路径需配置）

## 运行
```bash
mvn spring-boot:run    # 端口 18047
```

## 接口
| 方法 | 路径 | 说明 |
|---|---|---|
| GET | `/route/ask?question=` | 经 `ModelRouter.route()` 选择模型并同步问答 |

## 快速验证
```bash
curl "http://localhost:18047/route/ask?question=你好"
```

正常情况由主模型（DashScope qwen-plus）处理；主模型连续失败达到阈值后 starter `FallbackModelRouter` 自动切到 DeepSeek 备用（见 48-fallback-demo）。

## 源码导读
| 类 | 职责 | 教程小节 |
|---|---|---|
| `RoutingController` | 注入 `ModelRouter`，`route()` + `ChatClient` + `AuditLoggingAdvisor` | §20.2 |
| `application.yml` | 双模型 Key + `saa.learning.primary/fallback-model` | §19 + §20 |

## 冒烟测试
```bash
# 需 AI_DASHSCOPE_API_KEY
mvn -f examples/47-routing-demo/pom.xml test
```
