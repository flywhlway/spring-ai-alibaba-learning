# 25-redis-vector-demo

Redis **Stack**（`redis/redis-stack-server`）后端的 `VectorStore` 演示：文档入库、相似度检索与
语义缓存 TTL（对应教程第 11 章选型表 + Redis Stack 警告）。

> **禁止**直接复用 `core` profile 的 `redis:7.4-alpine`——普通 Redis 缺少 RediSearch / RedisJSON，
> 向量索引与检索会失败。本 Demo **必须**使用 Redis Stack。

## 前置条件
- 中间件：**Redis Stack**（端口 **6380**，避免与 core 普通 Redis 的 6379 冲突）
- 环境变量：`AI_DASHSCOPE_API_KEY`

### 启动 Redis Stack（二选一）

**方式 A：本目录 override 编排（推荐）**
```bash
cd examples/25-redis-vector-demo
docker compose -f docker-compose.override.yml up -d
```

**方式 B：手动一行命令**
```bash
docker run -d --name saa-redis-stack -p 6380:6379 redis/redis-stack-server:latest
```

确认镜像为 `redis/redis-stack-server`，**不要**使用 `redis:7.4-alpine`。

## 运行
```bash
mvn spring-boot:run    # 端口 18025，连接 localhost:6380
```

## 接口
| 方法 | 路径 | 说明 |
|---|---|---|
| POST | `/documents` | `VectorStore.add` 入库 |
| GET | `/search?q=&topK=` | 相似度检索 |
| GET | `/search/filter?q=&topK=&department=` | Metadata Filter |
| POST | `/cache` | 语义缓存写入（query 向量化，answer + TTL 进 metadata） |
| GET | `/cache/lookup?q=&similarityThreshold=` | 语义缓存查找（校验 expiresAt） |

## 快速验证
```bash
curl -X POST http://localhost:18025/documents \
  -H 'Content-Type: application/json' \
  -d '{"content":"高频 FAQ：如何重置车机密码","metadata":{"department":"cs"}}'

curl "http://localhost:18025/search?q=车机密码重置&topK=3"

curl -X POST http://localhost:18025/cache \
  -H 'Content-Type: application/json' \
  -d '{"query":"如何重置车机密码","answer":"进入设置-账户-重置密码","ttlSeconds":300}'

curl "http://localhost:18025/cache/lookup?q=车机密码怎么重置"
```

## 源码导读
| 类 / 文件 | 职责 |
|---|---|
| `RedisVectorController` | add / search / 语义缓存 TTL |
| `docker-compose.override.yml` | `redis/redis-stack-server` → 6380 |
| `application.yml` | `spring.data.redis.port=6380` |

## 运行结果
截图存放于 `images/examples/25-redis-vector-demo/`（真机运行后补充）。
