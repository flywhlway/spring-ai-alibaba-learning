# scripts —— 工程辅助脚本

| 脚本 | 用途 |
|---|---|
| `env-check.sh` | 开发环境自检：JDK 21 / Maven / OrbStack Docker / API Key 环境变量 / 常用端口 |
| `setup-env.example.sh` | 模型 API Key 环境变量模板。复制为 `setup-env.local.sh`（已被 .gitignore 排除）后填入真实 Key，`source` 加载 |
| `infra.sh` | 中间件生命周期封装：`up <profiles...>` / `down` / `clean` / `ps` |

## 首次使用

```bash
bash scripts/env-check.sh
cp scripts/setup-env.example.sh scripts/setup-env.local.sh
vi scripts/setup-env.local.sh
source scripts/setup-env.local.sh
bash scripts/infra.sh up core
```

安全铁律：任何真实 Key 只存在于 `setup-env.local.sh` 或 shell 环境，严禁写入 application.yml / 代码 / 文档。
