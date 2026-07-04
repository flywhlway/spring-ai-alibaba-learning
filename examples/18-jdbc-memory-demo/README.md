# 18-jdbc-memory-demo

`JdbcChatMemoryRepository` + `MessageChatMemoryAdvisor`：会话隔离与重启后持久化
（对应教程第 08 章可运行 Demo 权威规格）。

## 前置条件
- 中间件：`bash scripts/infra.sh up core`（PostgreSQL）
- 环境变量：`AI_DASHSCOPE_API_KEY`

## 运行
```bash
mvn spring-boot:run    # 端口 18018
```

## 接口
| 方法 | 路径 | 说明 |
|---|---|---|
| GET | `/chat?message=&userId=` | 按 userId 隔离的多轮对话 |

## 快速验证
```bash
curl "http://localhost:18018/chat?message=我叫Alice，请记住&userId=alice"
curl "http://localhost:18018/chat?message=我叫什么名字？&userId=alice"
curl "http://localhost:18018/chat?message=我叫什么名字？&userId=bob"
# Ctrl+C 重启后再问 alice，应仍记得
```

## 源码导读
| 类 | 职责 | 教程小节 |
|---|---|---|
| `MemoryConfig` | JdbcChatMemoryRepository + MessageWindow | 可运行 Demo |
| `MemoryController` | `ChatMemory.CONVERSATION_ID` | §8.x |

## 运行结果
截图存放于 `images/examples/18-jdbc-memory-demo/`（真机运行后补充）。
