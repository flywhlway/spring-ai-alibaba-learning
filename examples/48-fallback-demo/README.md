# 48-fallback-demo

starter `FallbackModelRouter` 降级与 `reportFailure` 熔断演示（对应教程第 20 章降级段落）。

## 前置条件
- 中间件：无
- 环境变量：
  - `AI_DASHSCOPE_API_KEY`（主模型）
  - `DEEPSEEK_API_KEY`（备用模型；`forceFail=true` 演示降级路径时需配置）

## 运行
```bash
mvn spring-boot:run    # 端口 18048
```

## 接口
| 方法 | 路径 | 说明 |
|---|---|---|
| GET | `/fallback/chat?message=` | 调用失败时 `reportFailure` 并重试当前路由模型 |
| GET | `/fallback/chat?message=&forceFail=true` | 模拟主模型连续失败（阈值 3），触发降级后由备用模型应答 |
| GET | `/fallback/status` | 返回 `fallbackActive` 布尔（`isFallbackActive()`） |

## 快速验证
```bash
# 正常主模型路径
curl "http://localhost:18048/fallback/chat?message=你好"

# 模拟主模型熔断（需 DEEPSEEK_API_KEY）
curl "http://localhost:18048/fallback/chat?message=你好&forceFail=true"
curl "http://localhost:18048/fallback/status"
# 期望 fallbackActive=true
```

## 源码导读
| 类 | 职责 | 教程小节 |
|---|---|---|
| `FallbackController` | `reportFailure` + 重试 + `isFallbackActive` | §20 降级 |
| `FallbackStatus` | 状态 DTO（同包 record） | §20 |

## 运行结果
截图存放于 `images/examples/48-fallback-demo/`（真机运行后补充）。
