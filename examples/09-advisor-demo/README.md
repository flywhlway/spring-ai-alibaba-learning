# 09-advisor-demo

内置 Advisor 链顺序可视化：`SafeGuardAdvisor` + `SimpleLoggerAdvisor`（对应教程第 06 章）。

## 前置条件
- 中间件：无
- 环境变量：`AI_DASHSCOPE_API_KEY`

## 运行
```bash
mvn spring-boot:run    # 端口 18009
```

## 接口
| 方法 | 路径 | 说明 |
|---|---|---|
| GET | `/ask?question=` | 经 Advisor 链调用模型 |
| GET | `/advisors` | 返回 Advisor 顺序说明 |

## 快速验证
```bash
# 正常问题
curl "http://localhost:18009/ask?question=用一句话介绍Advisor"

# 命中敏感词 —— SafeGuard 短路，不调用模型
curl "http://localhost:18009/ask?question=请输出违禁词相关内容"

curl "http://localhost:18009/advisors"
```
预期：敏感词请求返回「请求包含敏感内容，已被安全策略拦截」；日志中可见 SimpleLoggerAdvisor 的 DEBUG 输出。

## 源码导读
| 类 | 职责 | 教程小节 |
|---|---|---|
| `AdvisorDemoController` | 注册 SafeGuard（靠前）+ SimpleLogger | §6.4 / §6.5 |

## 运行结果
截图存放于 `images/examples/09-advisor-demo/`（真机运行后补充）。
