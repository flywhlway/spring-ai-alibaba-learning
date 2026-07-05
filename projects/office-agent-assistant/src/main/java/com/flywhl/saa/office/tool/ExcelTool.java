package com.flywhl.saa.office.tool;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;
import com.alibaba.excel.EasyExcel;
import com.flywhl.saa.office.config.OfficeProperties;

@Component
public class ExcelTool {
    private final Path workspace;
    public ExcelTool(OfficeProperties properties) throws IOException {
        this.workspace = Path.of(properties.tool().excel().workspace());
        Files.createDirectories(workspace);
    }
    @Tool(description = "生成 Excel 文件（表头+行数据 JSON 数组）")
    public String generateExcel(@ToolParam(description = "文件名") String fileName,
            @ToolParam(description = "表头，逗号分隔") String headers,
            @ToolParam(description = "行 JSON 二维数组") String rowsJson,
            ToolContext toolContext) {
        if (ToolSecuritySupport.requireRole(toolContext, "EMPLOYEE", "ADMIN") == null) return "权限不足";
        try {
            String safeName = fileName.replaceAll("[^a-zA-Z0-9._-]", "_");
            Path file = workspace.resolve(safeName.endsWith(".xlsx") ? safeName : safeName + ".xlsx");
            String[] headerArr = headers.split(",");
            List<List<String>> head = new ArrayList<>();
            for (String h : headerArr) head.add(List.of(h.trim()));
            List<List<String>> data = parseRows(rowsJson, headerArr.length);
            EasyExcel.write(file.toFile()).head(head).sheet("Sheet1").doWrite(data);
            return "已生成：" + file.toAbsolutePath();
        } catch (Exception ex) {
            return "Excel 生成失败：" + ex.getMessage();
        }
    }
    @SuppressWarnings("unchecked")
    private List<List<String>> parseRows(String rowsJson, int cols) {
        try {
            var mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            List<List<Object>> raw = mapper.readValue(rowsJson, List.class);
            List<List<String>> data = new ArrayList<>();
            for (List<Object> row : raw) {
                List<String> line = new ArrayList<>();
                for (int i = 0; i < cols; i++) {
                    line.add(i < row.size() && row.get(i) != null ? String.valueOf(row.get(i)) : "");
                }
                data.add(line);
            }
            return data;
        } catch (Exception ex) {
            return List.of(List.of("解析 rowsJson 失败"));
        }
    }
}

