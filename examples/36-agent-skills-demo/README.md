# 36-agent-skills-demo

`ClasspathSkillRegistry` + `SkillsAgentHook` / `SkillsInterceptor` 渐进式披露（对应教程第 13 章 §13.6）。

## 前置条件
- 中间件：无
- 环境变量：`AI_DASHSCOPE_API_KEY`

## 运行
```bash
mvn spring-boot:run    # 端口 18036
```

## 接口
| 方法 | 路径 | 说明 |
|---|---|---|
| GET | `/agent/skills?query=` | Agent 先 `read_skill` 加载 SKILL.md，再按技能指引回答 |

## 快速验证
```bash
curl "http://localhost:18036/agent/skills?query=车辆报P0420故障码该怎么排查？"
```
预期：模型先加载 `vehicle-diagnosis` 技能，再给出结构化排查建议（`Result` 包装）。

## Skills 资源布局
```
src/main/resources/skills/
  vehicle-diagnosis/SKILL.md
  ota-troubleshoot/SKILL.md
```
每个 `SKILL.md` 含 YAML frontmatter（`name` / `description`），目录名与 `name` 一致。

## 源码导读
| 类 | 职责 |
|---|---|
| `SkillsAgentConfig` | `ClasspathSkillRegistry` + `SkillsAgentHook`（禁止 `Skill.of`） |
| `SkillsAgentController` | `agent.call` → `Result` |

## 运行结果
截图存放于 `images/examples/36-agent-skills-demo/`（真机运行后补充）。
