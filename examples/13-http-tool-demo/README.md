# 13-http-tool-demo

外部 HTTP API 封装为 `@Tool`：`RestClient` 调用行情接口（对应教程第 07 章 §7.6）。

本 Demo 内置 `MockStockApiController` 模拟第三方服务，无需外网。

## 前置条件
- 中间件：无
- 环境变量：`AI_DASHSCOPE_API_KEY`

## 运行
```bash
mvn spring-boot:run    # 端口 18013
```

## 接口
| 方法 | 路径 | 说明 |
|---|---|---|
| GET | `/ask?question=` | 模型通过 StockPriceTools 查询行情 |
| GET | `/mock/quote?symbol=` | 模拟外部行情 API（工具内部调用） |

## 快速验证
```bash
curl "http://localhost:18013/mock/quote?symbol=AAPL"
curl "http://localhost:18013/ask?question=查询AAPL的最新股价"
```

## 源码导读
| 类 | 职责 | 教程小节 |
|---|---|---|
| `StockPriceTools` | RestClient + `@Tool` | §7.6 |
| `MockStockApiController` | 自包含模拟外部 API | Demo 工程化 |

## 运行结果
截图存放于 `images/examples/13-http-tool-demo/`（真机运行后补充）。
