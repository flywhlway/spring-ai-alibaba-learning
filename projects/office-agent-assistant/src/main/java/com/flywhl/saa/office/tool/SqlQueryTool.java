package com.flywhl.saa.office.tool;

import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;
import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import com.flywhl.saa.office.config.OfficeProperties;

@Component
public class SqlQueryTool {
    private static final Pattern SELECT_ONLY = Pattern.compile("^\s*select\s+", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
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
        if (sql == null || sql.isBlank()) return "SQL 不能为空";
        String normalized = sql.trim();
        if (!SELECT_ONLY.matcher(normalized).find()) return "仅允许 SELECT 查询";
        String lower = normalized.toLowerCase(Locale.ROOT);
        if (lower.contains(";") || lower.contains("--") || lower.contains("/*")) return "SQL 包含不允许的字符";
        List<String> allowed = properties.tool().sql().allowedTables();
        boolean tableOk = allowed.stream().anyMatch(t -> lower.contains(" " + t.toLowerCase(Locale.ROOT) + " ")
                || lower.contains(" " + t.toLowerCase(Locale.ROOT)));
        if (!tableOk) return "SQL 未命中白名单表：" + allowed;
        int maxRows = properties.tool().sql().maxRows();
        String limited = normalized + (lower.contains("limit") ? "" : " LIMIT " + maxRows);
        try {
            List<java.util.Map<String, Object>> rows = jdbcTemplate.queryForList(limited);
            return rows.isEmpty() ? "查询无结果" : rows.toString();
        } catch (Exception ex) {
            return "查询失败：" + ex.getMessage();
        }
    }
}

