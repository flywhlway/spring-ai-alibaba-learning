---
status: diagnosed
trigger: "CUSTOMER JWT 访问 /api/admin/dashboard/stats 返回 HTTP 500（AuthorizationDeniedException → GlobalExceptionHandler INTERNAL_ERROR），期望 403"
created: 2026-07-18T00:24:00+08:00
updated: 2026-07-18T00:26:00+08:00
goal: find_root_cause_only
symptoms_prefilled: true
phase: 06-smart-cs-platform
test: 4
severity: major
---

## Current Focus

hypothesis: "DashboardAdminController @PreAuthorize 拒绝 CUSTOMER 时抛出 AuthorizationDeniedException；该方法安全异常发生在 DispatcherServlet/AOP 内，不经 ExceptionTranslationFilter；common GlobalExceptionHandler 的 @ExceptionHandler(Exception.class) 将其兜底为 HTTP 500 INTERNAL_ERROR"
test: "静态证据链：SecurityConfig 仅 authenticated、无 AccessDenied 专用 handler；GlobalExceptionHandler 无 AuthorizationDenied/AccessDenied 分支；common 无 spring-security 依赖"
expecting: "根因确认：RBAC 拒绝机制本身正确，错误在异常→HTTP 状态映射"
next_action: "return ROOT CAUSE FOUND（find_root_cause_only，不修代码）"

reasoning_checkpoint:
  hypothesis: "AuthorizationDeniedException from @PreAuthorize is caught by GlobalExceptionHandler.handleUnexpected → HTTP 500 instead of 403"
  confirming_evidence:
    - "UAT/脚本：CUSTOMER 访问 admin stats 期望 403 实得 500；日志 AuthorizationDeniedException: Access Denied 经 GlobalExceptionHandler 记为未预期异常"
    - "GlobalExceptionHandler 仅有 BizException/校验/NotReadable + catch-all Exception→500；无 AccessDenied/AuthorizationDenied 专用 handler"
    - "SecurityFilterChain 对 /api/** 仅 .authenticated()；ADMIN 仅靠类级 @PreAuthorize；无 AccessDeniedHandler；全仓库 projects 无 AccessDenied ExceptionHandler"
    - "common/pom.xml 无 spring-security，无法在 SSOT handler 中直接引用 Security 异常类型（除非 optional 依赖或应用侧 handler）"
  falsification_test: "若去掉 @ExceptionHandler(Exception.class) 或先加 AuthorizationDenied→403 handler 后 CUSTOMER 仍返回 500，则本假设错误"
  fix_rationale: "N/A — diagnose only；建议方向见 Resolution"
  blind_spots: "未在本轮实机 curl 复现（依赖 UAT 日志与代码静态证据）；Nacos Data ID 落盘属同 Test 4 另一子问题，非本 RBAC 状态码根因"

## Symptoms

expected: CUSTOMER JWT 访问 GET /api/admin/dashboard/stats 返回 HTTP 403 Forbidden（RBAC 拒绝，非 500）；admin 访问成功且 stats 含会话/工单/cacheHitRate/成本字段
actual: admin GET 成功；CUSTOMER 访问同接口返回 HTTP 500（AuthorizationDeniedException 被 GlobalExceptionHandler 兜底成 INTERNAL_ERROR）
errors: org.springframework.security.authorization.AuthorizationDeniedException: Access Denied — logged by GlobalExceptionHandler as 未预期异常
reproduction: Test 4 in .planning/phases/06-smart-cs-platform/06-HUMAN-UAT.md；uat-smart-cs.sh RBAC check（customer → admin stats 期望 code=403）
started: Discovered during auto UAT 2026-07-18

## Eliminated

- hypothesis: "JWT role claim 未映射导致权限判断失败/错乱"
  evidence: "admin 同接口成功；SecurityConfig JwtGrantedAuthoritiesConverter 将 role claim → ROLE_*；失败类型为 AuthorizationDeniedException（拒绝），说明认证与角色解析正常，仅 CUSTOMER 缺 ADMIN 角色"
  timestamp: 2026-07-18T00:25:00+08:00

- hypothesis: "SecurityFilterChain 未配置导致完全无 RBAC"
  evidence: "@EnableMethodSecurity + DashboardAdminController @PreAuthorize(hasRole('ADMIN')) 确实拒绝；问题是拒绝后的 HTTP 状态码映射，不是 RBAC 未生效"
  timestamp: 2026-07-18T00:25:00+08:00

- hypothesis: "Nacos 3.2.2 v1 API 404 导致本 gap"
  evidence: "Nacos Data ID 落盘是 Test 4 并列验收项，与 CUSTOMER→500 状态码无关；本诊断仅覆盖 RBAC 403 vs 500"
  timestamp: 2026-07-18T00:25:00+08:00

## Evidence

- timestamp: 2026-07-18T00:24:30+08:00
  checked: 06-HUMAN-UAT.md Test 4 + Gaps
  found: "CUSTOMER 期望 403 实得 500；AuthorizationDeniedException 被 GlobalExceptionHandler 兜底；severity major；debug_session 空"
  implication: "症状与异常类型已由 UAT 钉死，调查焦点是异常→HTTP 映射"

- timestamp: 2026-07-18T00:24:45+08:00
  checked: GlobalExceptionHandler.java
  found: "handleUnexpected(Exception) @ResponseStatus(INTERNAL_SERVER_ERROR) 兜底所有未列异常；无 AccessDeniedException / AuthorizationDeniedException handler"
  implication: "AuthorizationDeniedException 命中兜底 → 500 + INTERNAL_ERROR"

- timestamp: 2026-07-18T00:25:00+08:00
  checked: DashboardAdminController.java
  found: "类级 @PreAuthorize(\"hasRole('ADMIN')\")；GET /stats 无额外安全注解"
  implication: "CUSTOMER 合法 JWT 过 URL authenticated 后在方法安全层被拒，抛 AuthorizationDeniedException"

- timestamp: 2026-07-18T00:25:15+08:00
  checked: SecurityConfig.java
  found: "@EnableMethodSecurity；/api/** 仅 authenticated；无 authorizeHttpRequests hasRole('ADMIN')；无 exceptionHandling/AccessDeniedHandler；oauth2ResourceServer jwt"
  implication: "角色拒绝只发生在方法安全 AOP（DispatcherServlet 内），ExceptionTranslationFilter 不会把它变成默认 403 响应；若无 MVC handler，或被 catch-all 吃掉"

- timestamp: 2026-07-18T00:25:30+08:00
  checked: common/pom.xml + projects 全库 Grep AccessDenied/AuthorizationDenied ExceptionHandler
  found: "common 仅 optional web+validation，无 spring-security；projects 内零 AccessDenied 专用 ExceptionHandler"
  implication: "当前无任何路径把方法安全拒绝映射为 403"

- timestamp: 2026-07-18T00:25:45+08:00
  checked: uat-smart-cs.sh RBAC 断言
  found: "customer Bearer 调 /api/admin/dashboard/stats 断言 HTTP 403"
  implication: "验收契约明确要 403；现状 500 即 gap"

- timestamp: 2026-07-18T00:26:00+08:00
  checked: .planning/debug/knowledge-base.md
  found: "文件不存在 / 无 active sessions"
  implication: "无已知模式可优先复用"

## Resolution

root_cause: |
  Spring Security 6 方法级 @PreAuthorize 拒绝时抛出 AuthorizationDeniedException（在 Controller AOP 内，过滤链 ExceptionTranslationFilter 之后）。
  common GlobalExceptionHandler 的 @ExceptionHandler(Exception.class) 将其当作未预期异常，映射为 HTTP 500 + INTERNAL_ERROR。
  RBAC 拒绝本身正确；缺陷是异常→HTTP 状态缺少 AccessDenied/AuthorizationDenied → 403 的专用处理（且 common 模块故意不依赖 spring-security）。
fix: ""
verification: ""
files_changed: []
