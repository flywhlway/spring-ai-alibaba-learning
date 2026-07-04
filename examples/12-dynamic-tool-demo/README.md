# 12-dynamic-tool-demo

运行时动态注册 `ToolCallback` 与异步 Tool（对应教程第 07 章 §7.2）。

## 前置条件
- 中间件：无
- 环境变量：`AI_DASHSCOPE_API_KEY`

## 运行
```bash
mvn spring-boot:run    # 端口 18012
```

## 接口
| 方法 | 路径 | 说明 |
|---|---|---|
| GET | `/tool/dynamic?question=&enableCalculator=` | `enableCalculator=true` 时动态挂上计算器工具；异步库存工具始终可用 |

## 快速验证
```bash
# 仅异步库存工具
curl "http://localhost:18012/tool/dynamic?question=查询SKU-1001的库存"

# 动态启用计算器
curl "http://localhost:18012/tool/dynamic?question=计算3加5等于多少&enableCalculator=true"
```

## 源码导读
| 类 | 职责 | 教程小节 |
|---|---|---|
| `DynamicToolFactory` | `FunctionToolCallback` 编程式动态构造 | §7.2 |
| `AsyncTools` | 工具方法内异步编排 + 超时 | §7.x |
| `DynamicToolController` | 按请求参数决定是否挂载计算器 | §7.2 |

## 运行结果
截图存放于 `images/examples/12-dynamic-tool-demo/`（真机运行后补充）。
