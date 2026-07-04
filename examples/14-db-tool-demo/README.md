# 14-db-tool-demo

数据库查询 Tool 与 SQL 安全防护演示：`@Tool` 方法内部全部使用 `JdbcTemplate` 的
`?` 占位符参数化查询，用户/模型提供的入参永远作为绑定参数传入，绝不拼接进 SQL 字符串
（对应教程第 07 章 §7.7 与安全建议）。

## 前置条件
- 中间件：无（内嵌 H2 内存数据库，启动时通过 `schema.sql`/`data.sql` 自动建表 + 播种数据）
- 环境变量：`AI_DASHSCOPE_API_KEY`（仅 `/db/ask` 需要；直连接口 `/db/products*` 无需模型 Key）

> 生产环境可将 H2 替换为仓库 `docker/docker-compose.yml` 的 `core` profile 提供的
> MySQL/PostgreSQL（`bash scripts/infra.sh up core`），只需切换 `spring.datasource.*`
> 与驱动依赖，`ProductTools` 的参数化查询写法不受影响。

## 运行
```bash
mvn spring-boot:run    # 端口 18014
```

## 接口
| 方法 | 路径 | 说明 |
|---|---|---|
| GET | `/db/ask?question=` | 模型驱动查询，自动选择工具方法 |
| GET | `/db/products?category=` | 直连按分类查询（跳过模型） |
| GET | `/db/products/{id}` | 直连按 ID 查询（跳过模型） |

## 快速验证
```bash
# 直连查询：验证参数化 SQL 本身正确
curl "http://localhost:18014/db/products?category=手机"
curl "http://localhost:18014/db/products/1"
curl "http://localhost:18014/db/products/999"   # 不存在 → code=1003

# 模型驱动查询
curl "http://localhost:18014/db/ask?question=帮我查一下耳机类目有哪些商品"
```
预期输出（`/db/products/1`，节选）：
```json
{"code":0,"message":"success","data":{"id":1,"name":"ThinkPad X1 Carbon","category":"笔记本电脑","price":12999.00,"stock":15}}
```
`/db/products/999` 预期：`{"code":1003,"message":"商品不存在：id=999"}`（由 common 全局异常处理器统一转换）。

## 源码导读
| 类 | 职责 | 教程小节 |
|---|---|---|
| `ProductTools` | `@Tool` 声明式工具，`JdbcTemplate` 参数化查询防注入 | §7.7 / 安全建议 |
| `DbToolController` | 模型驱动 `/db/ask` + 直连验证接口 | §7.2 |
| `schema.sql` / `data.sql` | H2 内嵌建表与种子数据 | — |

## 运行结果
截图存放于 `images/examples/14-db-tool-demo/`（真机运行后补充）。
