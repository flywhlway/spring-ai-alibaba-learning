package com.flywhl.saa.office.tool;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.chat.model.ToolContext;
import org.springframework.jdbc.core.JdbcTemplate;

import com.flywhl.saa.office.config.OfficeProperties;

/**
 * SqlQueryTool 单元测试：白名单与 SELECT 校验。
 *
 * @author flywhl
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("SQL 查询工具")
class SqlQueryToolTest {

    @Mock
    private JdbcTemplate jdbcTemplate;

    private SqlQueryTool sqlQueryTool;

    @BeforeEach
    void setUp() {
        OfficeProperties properties = new OfficeProperties(
                null, null,
                new OfficeProperties.Tool(
                        new OfficeProperties.Sql(List.of("report_sales"), 10),
                        new OfficeProperties.Http(List.of("localhost"), java.time.Duration.ofSeconds(5)),
                        new OfficeProperties.Excel("/tmp")),
                null, null);
        sqlQueryTool = new SqlQueryTool(jdbcTemplate, properties);
    }

    @Test
    @DisplayName("非 SELECT 被拒绝")
    void rejectNonSelect() {
        ToolContext ctx = new ToolContext(Map.of("role", "EMPLOYEE"));
        assertThat(sqlQueryTool.querySql("DELETE FROM report_sales", ctx)).contains("仅允许 SELECT");
    }

    @Test
    @DisplayName("白名单表可查询")
    void allowWhitelistedTable() {
        when(jdbcTemplate.queryForList(anyString())).thenReturn(List.of(Map.of("region", "华东")));
        ToolContext ctx = new ToolContext(Map.of("role", "EMPLOYEE"));
        String result = sqlQueryTool.querySql("SELECT region FROM report_sales", ctx);
        assertThat(result).contains("华东");
    }

    @Test
    @DisplayName("注释绕过白名单被拒绝（HIGH 修复回归）")
    void rejectCommentBypass() {
        ToolContext ctx = new ToolContext(Map.of("role", "EMPLOYEE"));
        // 子串白名单时代：FROM 后是 sys_user，靠 `-- report_sales` 注释骗过 contains 检查
        assertThat(sqlQueryTool.querySql("SELECT * FROM sys_user -- report_sales", ctx))
                .contains("不允许的字符");
    }

    @Test
    @DisplayName("UNION 拖取非白名单表被拒绝（HIGH 修复回归）")
    void rejectUnionExfiltration() {
        ToolContext ctx = new ToolContext(Map.of("role", "EMPLOYEE"));
        assertThat(sqlQueryTool.querySql(
                "SELECT region FROM report_sales UNION SELECT password_hash FROM sys_user", ctx))
                .contains("不允许的关键字");
    }

    @Test
    @DisplayName("子查询/派生表拖取被拒绝（HIGH 修复回归）")
    void rejectSubquery() {
        ToolContext ctx = new ToolContext(Map.of("role", "EMPLOYEE"));
        assertThat(sqlQueryTool.querySql(
                "SELECT * FROM (SELECT password_hash FROM sys_user) x", ctx))
                .containsAnyOf("子查询", "白名单");
    }

    @Test
    @DisplayName("JOIN 非白名单表被拒绝（HIGH 修复回归）")
    void rejectJoinNonWhitelisted() {
        ToolContext ctx = new ToolContext(Map.of("role", "EMPLOYEE"));
        assertThat(sqlQueryTool.querySql(
                "SELECT s.region FROM report_sales s JOIN sys_user u", ctx))
                .contains("不在白名单");
    }

    @Test
    @DisplayName("表名藏进字符串字面量不被误判为命中白名单")
    void literalTableNameNotCounted() {
        ToolContext ctx = new ToolContext(Map.of("role", "EMPLOYEE"));
        // 字面量里出现 report_sales，但真实 FROM 是 sys_user，应被白名单拒绝
        assertThat(sqlQueryTool.querySql(
                "SELECT * FROM sys_user WHERE note = 'from report_sales'", ctx))
                .contains("不在白名单");
    }
}
