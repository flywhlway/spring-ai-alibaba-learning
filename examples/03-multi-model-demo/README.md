# 03-multi-model-demo

DashScope 与 DeepSeek 两个 `ChatModel` 在同一应用共存，通过两个具名 `ChatClient` Bean 显式路由
（对应教程第 04 章）。这是第 20 章"多模型路由/降级"的雏形。

## 前置条件
- 中间件：无
- 环境变量：`AI_DASHSCOPE_API_KEY`、`DEEPSEEK_API_KEY`（DeepSeek 通道运行必需；仅编译不需要）

## 运行
```bash
mvn spring-boot:run    # 端口 18003
```

## 接口
| 方法 | 路径 | 说明 |
|---|---|---|
| GET | `/chat/dashscope?message=` | 走 DashScope 通道 |
| GET | `/chat/deepseek?message=` | 走 DeepSeek 通道 |
| GET | `/chat/usage?message=` | DashScope 调用并回显 Token 用量 |

## 快速验证
```bash
curl "http://localhost:18003/chat/dashscope?message=用一句话解释什么是自动装配"
curl "http://localhost:18003/chat/deepseek?message=用一句话解释什么是自动装配"
curl "http://localhost:18003/chat/usage?message=你好"
```
预期输出（节选）：
```text
回答：你好！有什么我可以帮你的吗？

[token 用量] prompt=12, completion=8, total=20
```

## 源码导读
| 类 | 职责 | 教程小节 |
|---|---|---|
| `ChatClientConfig` | 用 `ChatClient.builder(ChatModel)` 静态工厂声明两个具名 Bean | §4.6 / 关键源码解读 |
| `MultiModelController` | 参数名匹配 Bean 名消歧；演示 `Usage` 读取 | §4.4 / §4.7 |

## 运行结果
截图存放于 `images/examples/03-multi-model-demo/`（真机运行后补充）。
