---
phase: 04-knowledge-qa-platform
reviewed: 2026-07-05T15:42:00Z
depth: quick
advisory: true
files_reviewed: 94
files_reviewed_list:
  - projects/knowledge-qa-platform/src/main/java/com/flywhl/saa/knowledgeqa/**/*.java
  - projects/knowledge-qa-platform/src/main/resources/application.yml
  - projects/knowledge-qa-platform/src/main/resources/prompts/*.st
  - projects/knowledge-qa-platform/src/test/java/com/flywhl/saa/knowledgeqa/**/*.java
  - projects/knowledge-qa-platform/src/test/resources/application-it.yml
  - projects/knowledge-qa-platform/src/test/resources/application-test.yml
  - projects/knowledge-qa-platform/src/test/resources/db/schema.sql
  - projects/knowledge-qa-platform/src/test/resources/db/data.sql
  - projects/knowledge-qa-platform/db/schema.sql
  - projects/knowledge-qa-platform/db/data.sql
findings:
  critical: 1
  warning: 2
  info: 2
  total: 5
status: issues_found
---

# Phase 04: Code Review Report

**Reviewed:** 2026-07-05T15:42:00Z  
**Depth:** quick（模式扫描）  
**Files Reviewed:** 94  
**Status:** issues_found（advisory-only，不阻塞发版）

## Summary

对 `projects/knowledge-qa-platform` 全量 `src/` 源码（含 main/test、配置与 SQL）执行 quick 深度审查：硬编码密钥、危险 API、调试残留、空 catch、注释死代码等模式扫描，并对安全配置做轻量核对。

**整体评估：** 未发现 `eval`/`exec`、TODO/FIXME、空 catch（无注释）或真实 API Key 硬编码；密钥均经 `${ENV}` 占位注入，测试配置使用 dummy key，符合仓库约定。主要风险集中在 **Actuator 指标端点匿名暴露** 与 **开发默认口令/JWT 密钥**——后者已在 YAML 注释中标注 dev-only，生产需显式注入环境变量。

> **审查模式：** advisory-only。以下发现供迭代参考，**不作为 Phase 04 合入/发版硬门槛**。

## Critical Issues

### CR-01: Actuator prometheus/metrics/info 匿名可访问

**File:** `projects/knowledge-qa-platform/src/main/java/com/flywhl/saa/knowledgeqa/config/SecurityConfig.java:56-61`  
**Issue:** `SecurityFilterChain` 仅对 `/actuator/health` 显式 `permitAll()`，其余 Actuator 路径（`/actuator/prometheus`、`/actuator/metrics`、`/actuator/info`）因 `.anyRequest().permitAll()` 落入匿名访问。`application.yml` 已暴露上述端点，攻击者可无认证拉取 JVM/HTTP/业务指标，存在信息泄露面（README 标注「内网」但仍依赖网络隔离）。  
**Fix:**
```java
.authorizeHttpRequests(auth -> auth
        .requestMatchers(HttpMethod.POST, "/api/auth/login").permitAll()
        .requestMatchers("/actuator/health").permitAll()
        .requestMatchers(DOC_PATHS.toArray(String[]::new)).permitAll()
        .requestMatchers("/actuator/**").hasRole("ADMIN")  // 或 IP 白名单 / 独立 management port
        .requestMatchers("/api/**").authenticated()
        .anyRequest().denyAll())
```

## Warnings

### WR-01: 多处开发默认密钥/口令（生产未注入时生效）

**File:** `projects/knowledge-qa-platform/src/main/resources/application.yml:24,105-106,120`  
**File:** `projects/knowledge-qa-platform/src/main/java/com/flywhl/saa/knowledgeqa/config/KqaProperties.java:22-23,50-51`  
**Issue:** 模式扫描命中 JWT secret、DB 密码、MinIO 凭据的 dev 默认值（如 `dev-only-kqa-jwt-secret-key-32bytes!!`、`saa123456`、`minioadmin`）。YAML 注释已说明生产须注入环境变量，但 `KqaProperties` 在 secret 为空时仍会回填同一 dev 密钥，部署遗漏 `KQA_JWT_SECRET` 时静默使用弱密钥。  
**Fix:** 生产 profile 启动时校验必需环境变量非空；或在非 `dev`/`local` profile 下禁止 fallback：
```java
// KqaProperties.Jwt compact constructor
if (secret == null || secret.isBlank()) {
    throw new IllegalStateException("kqa.security.jwt.secret 未配置");
}
```

### WR-02: 上传原始文件名直接拼入 MinIO objectKey

**File:** `projects/knowledge-qa-platform/src/main/java/com/flywhl/saa/knowledgeqa/admin/service/DocumentAdminService.java:190-193`  
**Issue:** `buildObjectKey` 将 `file.getOriginalFilename()` 原样追加到 object key。若客户端上传 `../../etc/passwd` 或含 `/`、`\` 的文件名，可能产生非预期对象路径或日志/审计混淆（MinIO 侧通常不逃逸 bucket，但 key 语义不可控）。  
**Fix:**
```java
private static String sanitizeFileName(String fileName) {
    String base = Paths.get(fileName).getFileName().toString();
    return base.replaceAll("[^a-zA-Z0-9._-]", "_");
}
// buildObjectKey 中使用 sanitizeFileName(fileName)
```

## Info

### IN-01: MinIO 删除失败被静默吞掉

**File:** `projects/knowledge-qa-platform/src/main/java/com/flywhl/saa/knowledgeqa/admin/service/DocumentAdminService.java:156-158`  
**Issue:** `delete()` 中 MinIO `removeObject` 异常被空 catch 忽略（仅有注释）。对象残留时 DB 已删，造成存储孤儿文件，且无日志可排查。  
**Fix:** 至少 `log.warn("MinIO 对象删除失败: {}", document.getMinioObject(), ex);`，或记录到审计表供运维补偿。

### IN-02: 演示数据使用 `{noop}` 明文密码

**File:** `projects/knowledge-qa-platform/db/data.sql:10-13`  
**Issue:** 演示账号 `admin/admin123` 等使用 `{noop}` 前缀，符合 DelegatingPasswordEncoder 演示约定且文件头已声明 dev-only。若生产误执行该 seed 脚本则存在明文等价风险。  
**Fix:** 生产部署跳过 `data.sql` 或改用 BCrypt hash；在 CI/部署脚本中显式排除演示 seed。

---

_Reviewed: 2026-07-05T15:42:00Z_  
_Reviewer: Claude (gsd-code-reviewer)_  
_Depth: quick · Advisory-only_
