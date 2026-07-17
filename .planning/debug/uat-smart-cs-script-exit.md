---
status: diagnosed
trigger: "uat-smart-cs.sh 在有 Key + 中间件就绪时 exit 0（HITL approve 已知 D-14 除外）— auto 复跑 10 pass / 2 fail：customer→admin 500≠403；handoff/approve 500（D-14）"
created: 2026-07-18T00:24:00+08:00
updated: 2026-07-18T00:30:00+08:00
symptoms_prefilled: true
goal: find_root_cause_only
---

## Current Focus

hypothesis: CONFIRMED — 脚本 exit 1 由两个独立失败叠加；(a) AuthorizationDenied→GlobalExceptionHandler 兜底 500（可行动新 gap）；(b) approve 硬断言未 soft-allow 已知 D-14，且 resume() 覆盖 InterruptionMetadata 为 placeholder 导致 500
test: 对照脚本断言 + GlobalExceptionHandler + RunnableConfig.Builder.resume/addHumanFeedback 字节码 + HumanInTheLoopHook.interrupt
expecting: 分离 (a) 产品契约缺陷 vs (b) 脚本/HITL 已知债
next_action: return ROOT CAUSE FOUND（find_root_cause_only，不修代码）

## Symptoms

expected: 脚本 exit 0；与 06-UAT.md 预期一致（HITL approve 已知 D-14 除外）
actual: 10 pass / 2 fail (customer admin RBAC status + handoff approve)
errors: |
  - GET /api/admin/dashboard/stats (customer) — 期望 403，实际 HTTP 500
  - POST /api/handoff/approve — HTTP 500 (IllegalArgumentException: Human feedback metadata must be of type InterruptionMetadata)
reproduction: bash projects/smart-cs-platform/scripts/uat-smart-cs.sh
started: auto UAT 2026-07-18 after JWT HS256 hotfix

## Eliminated

- hypothesis: "approve 500 仅因 CR-01 pending 未注册（期望 404）"
  evidence: "UAT 先跑 POST /handoff/start 成功（会 put pendingByThread）；若 pending==null 会抛 BizException NOT_FOUND → HTTP 200+code≠0，而非 IllegalArgumentException 500。实际错误串来自 HumanInTheLoopHook.interrupt 对 HUMAN_FEEDBACK 的类型检查失败。"
  timestamp: 2026-07-18T00:28:00+08:00

- hypothesis: "Security FilterChain 未认证导致 500"
  evidence: "CUSTOMER 已带有效 JWT；authorizeHttpRequests 仅要求 /api/** authenticated；角色拒绝来自 @PreAuthorize(hasRole('ADMIN')) 方法安全。"
  timestamp: 2026-07-18T00:28:00+08:00

## Evidence

- timestamp: 2026-07-18T00:25:00+08:00
  checked: uat-smart-cs.sh L143-152 RBAC 断言与 L197-207 approve 断言；exit 逻辑 L222-226
  found: customer→stats 硬要求 HTTP 403；approve 硬要求 200+code=0；任一 FAIL 则 exit 1；无 D-14 soft-allow 分支
  implication: 脚本契约与 HUMAN-UAT Test 5「HITL approve 已知 D-14 除外」不一致；即使只修 RBAC，approve 仍会拖垮 exit

- timestamp: 2026-07-18T00:26:00+08:00
  checked: DashboardAdminController @PreAuthorize + SecurityConfig + GlobalExceptionHandler
  found: 方法安全拒绝抛 AuthorizationDeniedException（extends AccessDeniedException）；handler 无 AccessDenied 专用分支，落入 @ExceptionHandler(Exception) → @ResponseStatus(500) INTERNAL_ERROR；FilterChain 无 exceptionHandling AccessDeniedHandler
  implication: RBAC 语义正确（拒绝发生），HTTP 契约错误（500≠403）— 与 Test 4 gap 同源；属可行动新 gap

- timestamp: 2026-07-18T00:27:00+08:00
  checked: HUMAN-UAT Test 3/5、STATE D-14、06-REVIEW CR-01
  found: Test 3 已将 approve 500 归入 D-14/CR-01「勿当新 gap」；Test 5 expected 写明「HITL approve 已知 D-14 除外」；STATE Pending 仍指向 HITL/approve 债务
  implication: approve 失败对 Test 5 应计为已知债 soft-allow，不应单独开新产品 gap

- timestamp: 2026-07-18T00:29:00+08:00
  checked: RunnableConfig$Builder.resume/addHumanFeedback（graph-core 1.1.2.2）与 HumanInTheLoopHook.interrupt（agent-framework 1.1.2.2）
  found: addHumanFeedback 写入 InterruptionMetadata；随后 resume() 用 String "placeholder" 覆盖同一 HUMAN_FEEDBACK 键；interrupt() 见非 InterruptionMetadata 即抛 IllegalArgumentException（与 UAT 错误原文一致）。HumanHandoffController.approve 调用顺序正是 addHumanFeedback→resume
  implication: 在 /handoff/start 已注册 pending 时，approve 500 的直接机制是 resume 覆盖反馈元数据（与 CR-01「pending 空→404」不同路径，但仍属 HITL 债务族，HUMAN-UAT 已归 D-14）

## Resolution

root_cause: |
  uat-smart-cs.sh exit≠0 是两个独立失败的合取：
  (a)【可行动新 gap / 与 Test 4 同源】CUSTOMER 访问 @PreAuthorize(ADMIN) 接口时 AuthorizationDeniedException 被 GlobalExceptionHandler 兜底成 HTTP 500，脚本断言 403 失败。
  (b)【已知 D-14 债 + 脚本未 soft-allow】approve 硬失败计入 FAIL；HUMAN-UAT 明确除外。观察到的 500 机制为 Builder.resume() 覆盖 addHumanFeedback 写入的 InterruptionMetadata（非 CR-01 pending 缺失路径）。
fix: （diagnose-only，未应用）
verification: （diagnose-only）
files_changed: []
