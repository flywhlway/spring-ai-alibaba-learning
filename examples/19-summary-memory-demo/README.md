# 19-summary-memory-demo

摘要压缩长对话：自定义 `SummaryCompressingAdvisor` 在消息数达到阈值时，
用模型把旧历史压成一段摘要再写回 `ChatMemory`（对应教程第 08 章 FAQ）。

## 前置条件
- 中间件：无
- 环境变量：`AI_DASHSCOPE_API_KEY`

## 运行
```bash
mvn spring-boot:run    # 端口 18019
```

## 接口
| 方法 | 路径 | 说明 |
|---|---|---|
| GET | `/chat?message=&conversationId=` | 多轮对话（默认 conversationId=demo） |
| GET | `/history?conversationId=` | 查看当前记忆（观察【历史摘要】） |

## 快速验证
连续发送 6 条以上消息后查看 history，应出现 `SYSTEM` 角色且 content 以 `【历史摘要】` 开头：
```bash
for i in 1 2 3 4 5 6; do
  curl -s "http://localhost:18019/chat?message=这是第${i}轮，我喜欢数字${i}&conversationId=demo"
  echo
done
curl "http://localhost:18019/history?conversationId=demo"
```

## 源码导读
| 类 | 职责 |
|---|---|
| `SummaryCompressingAdvisor` | 阈值触发、裸 ChatModel 摘要、回写记忆 |
| `MemoryConfig` | 摘要 Advisor order 早于 MessageChatMemoryAdvisor |

## 运行结果
截图存放于 `images/examples/19-summary-memory-demo/`（真机运行后补充）。
