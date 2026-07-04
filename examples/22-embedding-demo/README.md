# 22-embedding-demo

`EmbeddingModel` 维度与成本基准测试：对比 `text-embedding-v4` 在 64/256/1024/2048 维下的
调用耗时、向量长度与 float32 存储字节（对应教程第 10 章）。

## 前置条件
- 中间件：**无**
- 环境变量：`AI_DASHSCOPE_API_KEY`

## 运行
```bash
mvn spring-boot:run    # 端口 18022
```

## 接口
| 方法 | 路径 | 说明 |
|---|---|---|
| GET | `/embedding/benchmark?text=` | 多维度向量化基准：耗时 / 维度 / 存储字节 / token |

## 快速验证
```bash
curl "http://localhost:18022/embedding/benchmark?text=车辆OTA升级失败常见原因分析"
```

预期输出（节选，延迟因网络而异）：
```json
{
  "code": 0,
  "message": "success",
  "data": {
    "results": [
      {"dimensions": 64, "vectorLength": 64, "costMs": 210, "storageBytesPerVector": 256, "estimatedTokenUsage": 12},
      {"dimensions": 1024, "vectorLength": 1024, "costMs": 203, "storageBytesPerVector": 4096, "estimatedTokenUsage": 12}
    ]
  }
}
```

**关键观察**：不同维度的调用延迟基本一致，但存储成本线性增长——维度选型的核心是存储与检索计算成本，而非 API 延迟。

## 源码导读
| 类 | 职责 | 教程章节 |
|---|---|---|
| `EmbeddingBenchmarkController` | 多维度 `EmbeddingRequest` + Usage 读取 | §10.5–10.7 |
| `application.yml` | 全局默认 `text-embedding-v4` / `dimensions: 1024` | §10.6 |

## 运行结果
截图存放于 `images/examples/22-embedding-demo/`（真机运行后补充）。
