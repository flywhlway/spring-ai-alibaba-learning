# scripts —— 工程辅助脚本

| 脚本 | 用途 |
|---|---|
| `env-check.sh` | 开发环境自检：JDK 21 / Maven / OrbStack Docker / API Key / 常用端口 |
| `setup-env.example.sh` | 模型 API Key 模板；复制为 `setup-env.local.sh`（gitignore）后 `source` |
| `infra.sh` | 中间件生命周期：`up <profiles...>` / `down` / `clean` / `ps` |
| `version-audit.sh` | BOM / 依赖版本对齐自检 |
| `spring-ai-2-readiness.sh` | Spring AI 2.0 破坏点扫描 |
| `quality-gate.sh` | 本地 / CI 共用质量门禁 |
| `uat-phase3.sh` | Phase 3：48 Demo 抽样 / UAT 入口 |
| `uat-knowledge-qa.sh` | Phase 4：知识库问答平台 UAT |
| `nacos-init-dev.sh` | 开发环境 Nacos 初始化辅助 |
| `deploy-smoke.sh` | 部署冒烟（可选；见生产化文档） |

企业项目自带 UAT（不放本目录）：

- `projects/office-agent-assistant/scripts/uat-office-agent.sh`
- `projects/smart-cs-platform/scripts/uat-smart-cs.sh`

## 首次使用

```bash
cp scripts/setup-env.example.sh scripts/setup-env.local.sh
# 编辑 setup-env.local.sh 填入真实 Key
source scripts/setup-env.local.sh
bash scripts/env-check.sh
bash scripts/infra.sh up core
```

安全铁律：任何真实 Key 只存在于 `setup-env.local.sh` 或 shell 环境，严禁写入 application.yml / 代码 / 文档。

更多运维说明：[docs/00-overview/05-生产化与运维.md](../docs/00-overview/05-生产化与运维.md) · UAT 索引：[06-UAT债务索引.md](../docs/00-overview/06-UAT债务索引.md)。
