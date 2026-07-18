# 06 — UAT 债务索引

> Phase 7 可发现性索引（D-13）。**仅**汇总 Phase 3–6 的 UAT / HUMAN-UAT 入口与脚本；**不**在默认 CI 执行真机 UAT。

## 声明

- 本页是债务**发现入口**，不是执行结果；各 Phase 的 `*-UAT.md` / `*-HUMAN-UAT.md` 仍为债务 SSOT。
- **禁止**把下列脚本挂进 `.github/workflows/ci.yml`；真机路径保持 optional / 本地。
- 真机 UAT 需要：`AI_DASHSCOPE_API_KEY`（仅本地 env，勿提交）+ `bash scripts/infra.sh`（或项目 compose override）已起中间件。
- 不修改、不「关闭」既有 HUMAN-UAT 结论。

## 索引表

| Phase | 规划文档 | 脚本 | 端口 | 状态/备注 |
|-------|----------|------|------|-----------|
| 3 | [`.planning/phases/03-48-demo/03-UAT.md`](../../.planning/phases/03-48-demo/03-UAT.md) | [`scripts/uat-phase3.sh`](../../scripts/uat-phase3.sh) | `180NN`（Demo 编号映射） | 已 Verified；可链规划侧 UAT 结果 |
| 4 | [`04-UAT.md`](../../.planning/phases/04-knowledge-qa-platform/04-UAT.md) + [`04-HUMAN-UAT.md`](../../.planning/phases/04-knowledge-qa-platform/04-HUMAN-UAT.md) | [`scripts/uat-knowledge-qa.sh`](../../scripts/uat-knowledge-qa.sh) | `19100` | **已关闭**（2026-07-18 脚本 UAT 8/0，含 Key） |
| 5 | [`05-HUMAN-UAT.md`](../../.planning/phases/05-office-agent-assistant/05-HUMAN-UAT.md) | [`projects/office-agent-assistant/scripts/uat-office-agent.sh`](../../projects/office-agent-assistant/scripts/uat-office-agent.sh) + [项目 README「测试」节](../../projects/office-agent-assistant/README.md) | `19200` | **已关闭**（2026-07-18 脚本 UAT 12/0，含 Key） |
| 6 | [`06-UAT.md`](../../.planning/phases/06-smart-cs-platform/06-UAT.md) + [`06-HUMAN-UAT.md`](../../.planning/phases/06-smart-cs-platform/06-HUMAN-UAT.md) | [`projects/smart-cs-platform/scripts/uat-smart-cs.sh`](../../projects/smart-cs-platform/scripts/uat-smart-cs.sh) | `19300` | HUMAN-UAT 5/5 + uat-smart-cs.sh exit 0（见 06-HUMAN-UAT） |

## 快速入口

```bash
source scripts/setup-env.local.sh && bash scripts/env-check.sh
bash scripts/infra.sh up core   # 按项目再叠 vector/search 等

bash scripts/uat-phase3.sh
bash scripts/uat-knowledge-qa.sh
bash projects/office-agent-assistant/scripts/uat-office-agent.sh
bash projects/smart-cs-platform/scripts/uat-smart-cs.sh
```

## 相关

- [05-生产化与运维.md](05-生产化与运维.md) — CI / quality-gate / Compose（真机 UAT 不进默认 CI）
- Phase 6 code-review Critical（D-14 HITL）已在 `06-REVIEW-FIX.md` 修复（8/8）；真机 HITL approve 硬断言见 uat-smart-cs.sh
