package com.flywhl.saa.office.tool;

import org.springaicommunity.mcp.annotation.McpTool;
import org.springaicommunity.mcp.annotation.McpToolParam;
import org.springframework.stereotype.Service;

@Service
public class OfficeMcpTools {
    private final SqlQueryTool sqlQueryTool;
    private final HttpInternalTool httpInternalTool;
    public OfficeMcpTools(SqlQueryTool sqlQueryTool, HttpInternalTool httpInternalTool) {
        this.sqlQueryTool = sqlQueryTool;
        this.httpInternalTool = httpInternalTool;
    }
    @McpTool(description = "MCP 报表 SQL 只读查询")
    public String mcpQuerySql(@McpToolParam(description = "SELECT 语句", required = true) String sql) {
        return sqlQueryTool.querySql(sql, null);
    }
    @McpTool(description = "MCP 内部 HTTP GET")
    public String mcpHttpGet(@McpToolParam(description = "URL", required = true) String url) {
        return httpInternalTool.httpGet(url, null);
    }
}

