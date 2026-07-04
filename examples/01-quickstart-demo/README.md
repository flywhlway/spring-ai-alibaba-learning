# 01-quickstart-demo

最小可运行 SAA 应用：验证 JDK 21 / DashScope Key / 网络出网全部就位（对应教程第 01 章）。

## 前置条件
- 中间件：无
- 环境变量：`AI_DASHSCOPE_API_KEY`

## 运行
```bash
mvn spring-boot:run    # 端口 18001
```

## 接口
| 方法 | 路径 | 说明 |
|---|---|---|
| GET | `/chat?message=` | 单轮问答，默认 message="用一句话介绍你自己" |

## 快速验证
```bash
curl "http://localhost:18001/chat?message=你好，介绍一下你自己"
```
预期输出（文本）：
```text
你好！我是通义千问，阿里云研发的超大规模语言模型...
```

## 源码导读
| 类 | 职责 | 教程小节 |
|---|---|---|
| `QuickstartApplication` | Spring Boot 启动入口 | §1 可运行 Demo |
| `ChatController` | 注入自动装配的 `ChatClient.Builder`，`.build()` 后单例复用 | §关键源码解读 |

## 运行结果
截图存放于 `images/examples/01-quickstart-demo/`（真机运行后补充）。
