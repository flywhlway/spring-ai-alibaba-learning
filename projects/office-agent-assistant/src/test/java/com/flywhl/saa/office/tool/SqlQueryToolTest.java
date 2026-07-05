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
}
