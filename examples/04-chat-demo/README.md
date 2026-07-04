# 04-chat-demo

`ChatClient` 全 Fluent API 演示：`system()/user()/options()` 链、三种响应提取、`Usage` 读取
（对应教程第 04 章）。统一返回 `Result<T>`，复用仓库 common 的错误响应结构。

## 前置条件
- 中间件：无
- 环境变量：`AI_DASHSCOPE_API_KEY`

## 运行
```bash
mvn spring-boot:run    # 端口 18004
```

## 接口
| 方法 | 路径 | 说明 |
|---|---|---|
| GET | `/chat/simple?message=` | `.call().content()` 只取文本 |
| POST | `/chat` | 系统提示 + 温度覆盖 + 返回 Token 用量 |

## 快速验证
```bash
curl "http://localhost:18004/chat/simple?message=你好"

curl -X POST http://localhost:18004/chat \
  -H 'Content-Type: application/json' \
  -d '{"system":"你是严谨的架构师","message":"用一句话解释三级配置覆盖","temperature":0.1}'
```
预期输出（POST，节选）：
```json
{"code":0,"message":"success","data":{"content":"...","model":"qwen-plus","promptTokens":28,"completionTokens":30,"totalTokens":58}}
```
空 message 触发校验：返回 `code=1000`（由 common 全局异常处理器统一转换）。

## 源码导读
| 类 | 职责 | 教程小节 |
|---|---|---|
| `ChatController` | Fluent 链、调用级 `DashScopeChatOptions` 覆盖、Usage 读取 | §4.5-4.8 |
| `ChatVO` | 承载文本 + 模型 + Token 用量 | §4.4 |
| `@Import(GlobalExceptionHandler)` | 复用 common 统一异常响应 | 架构规范 §5.2 |

## 运行结果
截图存放于 `images/examples/04-chat-demo/`（真机运行后补充）。
