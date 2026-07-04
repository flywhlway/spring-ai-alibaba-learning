# 17-redis-memory-demo

基于**普通 Redis**（非 Redis Stack）自定义 `ChatMemoryRepository`，实现会话记忆持久化
（对应教程第 08 章思考题 2 / FAQ）。

官方 `RedisChatMemoryRepository` 需要 Redis Stack（RedisJSON + RediSearch），本仓库
`core` profile 的 `redis:7.4-alpine` 不满足，故本 Demo 用 List + JSON 自实现存储层。

## 前置条件
- 中间件：`bash scripts/infra.sh up core`（Redis）
- 环境变量：`AI_DASHSCOPE_API_KEY`

## 运行
```bash
mvn spring-boot:run    # 端口 18017
```

## 接口
| 方法 | 路径 | 说明 |
|---|---|---|
| GET | `/chat?message=&userId=` | 带会话隔离的多轮对话 |

## 快速验证
```bash
curl "http://localhost:18017/chat?message=我叫Alice，请记住&userId=alice"
curl "http://localhost:18017/chat?message=我叫什么名字？&userId=alice"
# 重启应用后再问，应仍记得 Alice
curl "http://localhost:18017/chat?message=我叫什么名字？&userId=bob"
```

## 已知限制
- 仅持久化纯文本 USER/ASSISTANT/SYSTEM；TOOL 消息简化为 ASSISTANT 还原。

## 源码导读
| 类 | 职责 |
|---|---|
| `RedisChatMemoryRepository` | List + MessageDto JSON |
| `MemoryConfig` | MessageWindow + MessageChatMemoryAdvisor |

## 运行结果
截图存放于 `images/examples/17-redis-memory-demo/`（真机运行后补充）。
