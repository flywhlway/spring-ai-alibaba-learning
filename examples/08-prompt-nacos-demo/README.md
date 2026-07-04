# 08-prompt-nacos-demo

基于 Nacos 的 Prompt 热更新：`ConfigurablePromptTemplateFactory` + 默认模板兜底
（对应教程第 05 章「可运行 Demo：Prompt 模板 + Nacos 热更新」）。

## 前置条件
- 中间件：`bash scripts/infra.sh up cloud`（启动 Nacos）
- 环境变量：`AI_DASHSCOPE_API_KEY`

## 运行
```bash
mvn spring-boot:run    # 端口 18008
```

## 接口
| 方法 | 路径 | 说明 |
|---|---|---|
| GET | `/diagnosis?code=` | 用 `dtc-diagnosis` 模板渲染后调用模型；Nacos 无配置时走代码内默认模板 |

## 快速验证
```bash
# 第一次：Nacos 尚无配置，走默认模板
curl "http://localhost:18008/diagnosis?code=P0420"
```

登录 Nacos 控制台（<http://localhost:8080> 或 <http://localhost:8848/nacos>，账号/密码均为 `nacos`），新建配置：

| 项 | 值 |
|---|---|
| Data ID | `spring.ai.alibaba.configurable.prompt` |
| Group | `DEFAULT_GROUP` |
| 格式 | JSON |

```json
[
  {
    "name": "dtc-diagnosis",
    "template": "你是资深车辆故障诊断专家。请分析故障码 {code} 的可能原因，按严重程度排序列出前三项，并给出建议的排查步骤。"
  }
]
```

**不重启应用**，再次执行：
```bash
curl "http://localhost:18008/diagnosis?code=P0420"
```
第二次响应应更详细、更结构化。

## 源码导读
| 类 | 职责 | 教程小节 |
|---|---|---|
| `PromptNacosController` | `getTemplate` → 兜底 `create` → `render` → ChatClient | §5.5 / 可运行 Demo |
| `application.yml` | `spring.ai.nacos.prompt.template.enabled=true` + Nacos 地址 | §5.6 |

## 运行结果
截图存放于 `images/examples/08-prompt-nacos-demo/`（真机运行后补充）。
