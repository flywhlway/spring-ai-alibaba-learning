# 16-memory-demo

`ChatMemory` 新 API 演示：`MessageWindowChatMemory` 整轮次滑动窗口 + `InMemoryChatMemoryRepository`，
显式 `conversationId` 实现多会话隔离（对应教程第 08 章）。窗口大小设为 6（约 3 轮对话），
便于在少量请求内观察到旧消息被驱逐。

## 前置条件
- 中间件：无
- 环境变量：`AI_DASHSCOPE_API_KEY`

## 运行
```bash
mvn spring-boot:run    # 端口 18016
```

## 接口
| 方法 | 路径 | 说明 |
|---|---|---|
| POST | `/memory/chat` | 发送消息，请求体 `{conversationId, message}` |
| GET | `/memory/history/{conversationId}` | 查看该会话当前保留的历史消息 |
| DELETE | `/memory/history/{conversationId}` | 清空该会话记忆 |

## 快速验证
```bash
# alice 告知名字
curl -X POST http://localhost:18016/memory/chat -H 'Content-Type: application/json' \
  -d '{"conversationId":"alice","message":"我叫Alice，请记住"}'

# alice 追问 —— 应记得
curl -X POST http://localhost:18016/memory/chat -H 'Content-Type: application/json' \
  -d '{"conversationId":"alice","message":"我叫什么名字？"}'

# bob 追问同样问题 —— 不应该知道（会话隔离）
curl -X POST http://localhost:18016/memory/chat -H 'Content-Type: application/json' \
  -d '{"conversationId":"bob","message":"我叫什么名字？"}'

# 查看 alice 当前保留的历史（连续对话超过 3 轮后，最早的 UserMessage/AssistantMessage 会被整轮次驱逐）
curl http://localhost:18016/memory/history/alice
```
预期输出（节选）：
```json
{"code":0,"message":"success","data":"你叫 Alice。"}
```
bob 会话应回复"不知道你的名字"一类内容，证明两个 conversationId 的记忆互不干扰。

## 源码导读
| 类 | 职责 | 教程小节 |
|---|---|---|
| `MemoryConfig` | 组装 `MessageWindowChatMemory`（策略）+ `InMemoryChatMemoryRepository`（存储） | §8.1/§8.4 |
| `MemoryController` | 显式传递 `ChatMemory.CONVERSATION_ID`，暴露历史查看/清空接口 | §8.3 |

## 运行结果
截图存放于 `images/examples/16-memory-demo/`（真机运行后补充）。
