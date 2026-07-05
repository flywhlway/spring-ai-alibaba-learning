package com.flywhl.saa.office.tool;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import com.flywhl.saa.office.config.OfficeProperties;

/**
 * 报表只读 SQL 查询 Tool。
 *
 * <p>安全模型（模型产出的 SQL 一律视为不可信输入）：
 * <ol>
 *   <li>剥离字符串字面量后再做全部校验，防止把白名单表名藏进字面量绕过；</li>
 *   <li>仅允许单条 SELECT；拒绝注释、分号、反引号/双引号、危险关键字（UNION/INTO/OUTFILE 等）；</li>
 *   <li>FROM/JOIN 后的每一个表名必须与白名单精确相等（词法级提取，含 WHERE 子查询中的 FROM），
 *       拒绝派生表/子查询与 schema 限定名；</li>
 *   <li>纵深防御：生产环境应为本 Tool 配置仅被 GRANT 白名单表 SELECT 权限的只读数据库账号。</li>
 * </ol>
 *
 * @author flywhl
 */
@Component
public class SqlQueryTool {

    private static final Pattern SELECT_ONLY =
            Pattern.compile("^\\s*select\\s+", Pattern.CASE_INSENSITIVE);
    /** 剥离单引号字符串字面量（'' 转义按 SQL 标准处理）。 */
    private static final Pattern STRING_LITERAL = Pattern.compile("'(?:[^']|'')*'");
    /** 危险关键字：写操作 / 多语句 / 联合注入 / 文件与元数据访问 / 时间盲注原语。 */
    private static final Pattern DENIED_KEYWORDS = Pattern.compile(
            "\\b(insert|update|delete|drop|alter|create|grant|revoke|truncate|replace|merge|call|execute|handler"
                    + "|union|into|outfile|dumpfile|load_file|information_schema|performance_schema|sleep|benchmark)\\b",
            Pattern.CASE_INSENSITIVE);
    /** 提取 FROM/JOIN 之后的表引用（子查询会命中左括号分支被拒绝）。 */
    private static final Pattern TABLE_REF =
            Pattern.compile("\\b(?:from|join)\\s+(\\(|[a-zA-Z0-9_$.]+)", Pattern.CASE_INSENSITIVE);
    private static final Pattern HAS_LIMIT = Pattern.compile("\\blimit\\b", Pattern.CASE_INSENSITIVE);

    private final JdbcTemplate jdbcTemplate;
    private final OfficeProperties properties;

    public SqlQueryTool(JdbcTemplate jdbcTemplate, OfficeProperties properties) {
        this.jdbcTemplate = jdbcTemplate;
        this.properties = properties;
    }

    @Tool(description = "执行只读 SQL 查询（白名单表：report_sales/report_attendance/approval_request）")
    public String querySql(@ToolParam(description = "SELECT 语句") String sql, ToolContext toolContext) {
        if (ToolSecuritySupport.requireRole(toolContext, "EMPLOYEE", "ADMIN") == null) {
            return "权限不足：仅员工或管理员可查询报表";
        }
        if (sql == null || sql.isBlank()) {
            return "SQL 不能为空";
        }
        String normalized = sql.trim();
        String violation = validate(normalized);
        if (violation != null) {
            return violation;
        }
        int maxRows = properties.tool().sql().maxRows();
        String stripped = STRING_LITERAL.matcher(normalized).replaceAll("''");
        String limited = HAS_LIMIT.matcher(stripped).find() ? normalized : normalized + " LIMIT " + maxRows;
        try {
            List<java.util.Map<String, Object>> rows = jdbcTemplate.queryForList(limited);
            return rows.isEmpty() ? "查询无结果" : rows.toString();
        } catch (Exception ex) {
            return "查询失败：" + ex.getMessage();
        }
    }

    /**
     * 校验模型产出的 SQL，全部检查基于字面量剥离后的文本。
     *
     * @return 违规说明；合法时返回 {@code null}
     */
    private String validate(String normalized) {
        if (!SELECT_ONLY.matcher(normalized).find()) {
            return "仅允许 SELECT 查询";
        }
        // 未闭合的字符串字面量直接拒绝，防止截断式逃逸
        if (countUnescapedQuotes(normalized) % 2 != 0) {
            return "SQL 字符串字面量未闭合";
        }
        String stripped = STRING_LITERAL.matcher(normalized).replaceAll("''");
        String lower = stripped.toLowerCase(Locale.ROOT);
        if (lower.contains(";") || lower.contains("--") || lower.contains("/*") || lower.contains("#")
                || lower.contains("`") || lower.contains("\"") || lower.contains("\\")) {
            return "SQL 包含不允许的字符";
        }
        if (DENIED_KEYWORDS.matcher(lower).find()) {
            return "SQL 包含不允许的关键字";
        }
        List<String> allowed = properties.tool().sql().allowedTables().stream()
                .map(t -> t.toLowerCase(Locale.ROOT))
                .toList();
        List<String> tables = new ArrayList<>();
        Matcher m = TABLE_REF.matcher(lower);
        while (m.find()) {
            String ref = m.group(1);
            if ("(".equals(ref)) {
                return "不允许子查询/派生表";
            }
            if (ref.contains(".") || ref.contains("$")) {
                return "不允许 schema 限定或特殊表名：" + ref;
            }
            tables.add(ref);
        }
        if (tables.isEmpty()) {
            return "SQL 缺少 FROM 白名单表";
        }
        for (String table : tables) {
            if (!allowed.contains(table)) {
                return "表 " + table + " 不在白名单：" + allowed;
            }
        }
        return null;
    }

    private long countUnescapedQuotes(String sql) {
        return sql.chars().filter(c -> c == '\'').count();
    }
}
