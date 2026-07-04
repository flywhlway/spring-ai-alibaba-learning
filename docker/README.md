# docker —— 统一中间件编排

`docker-compose.yml` 是全仓库中间件的 SSOT，面向 Apple Silicon（OrbStack）验证，所有镜像提供 arm64。按 profile 分组，避免一次拉起全部容器挤占内存。

## profile 分组

| profile | 服务 | 端口 | 用途 |
|---|---|---|---|
| core | Redis | 6379 | Memory / 缓存 / 限流 |
| core | PostgreSQL (pgvector/pg16) | 5432 | 业务库 + 轻量向量（库 `saa_learning`，saa/saa123456） |
| core | MySQL 8.4 | 3306 | 业务库（root/root123456，saa/saa123456） |
| core, vector | MinIO | 9000 / 9001 | 对象存储（minioadmin/minioadmin），兼作 Milvus 后端 |
| vector | etcd + Milvus 2.5 | 19530 / 9091 | 主向量库 |
| mq | Kafka 3.9 (KRaft) | 9092 | 事件流 |
| mq | RabbitMQ 4.1 | 5672 / 15672 | 异步任务 |
| search | Elasticsearch 8.17 | 9200 | 全文/混合检索 |
| cloud | Nacos 3.0 | 8848 / 9848 / 8080(控制台) | 配置中心 / Prompt 热更新 / MCP Registry / A2A |

## 常用命令（推荐经 scripts/infra.sh）

```bash
bash scripts/infra.sh up core            # 日常开发最小集
bash scripts/infra.sh up core vector     # RAG 全链路
bash scripts/infra.sh up core cloud      # Nacos 相关章节
bash scripts/infra.sh ps                 # 状态
bash scripts/infra.sh down               # 停止（保留数据卷）
bash scripts/infra.sh clean              # 停止并删卷（二次确认）
```

## 内存参考（48GB M5 Pro 无压力）

core ≈ 1.5GB · vector ≈ +2.5GB · mq ≈ +1.5GB · search ≈ +1GB · cloud ≈ +1GB；全量约 7.5GB。

## 注意

- 所有账号密码仅限本机开发；生产安全配置见教程第 20 章；
- Nacos 3.x 默认开启鉴权，compose 内置了开发用固定 token（生产必须替换）；
- 企业项目的专属服务（如 Prometheus/Grafana）在各项目 `docker-compose.override.yml` 中叠加，不污染本文件。
