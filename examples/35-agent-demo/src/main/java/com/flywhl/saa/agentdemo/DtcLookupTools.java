package com.flywhl.saa.agentdemo;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * OBD-II 故障码查询工具，供 ReactAgent 在 ReAct 循环中调用。
 *
 * @author flywhl
 */
@Component
public class DtcLookupTools {

    private static final Map<String, String> DTC_DB = Map.of(
            "P0420", "三元催化转化器效率低于阈值（1号库）",
            "P0300", "随机/多缸失火",
            "P0171", "系统偏稀（1号库）");

    @Tool(description = "查询OBD-II标准故障码的官方定义")
    public String lookupDtc(@ToolParam(description = "故障码，如P0420") String code) {
        String normalized = code == null ? "" : code.trim().toUpperCase();
        return DTC_DB.getOrDefault(normalized, "未知故障码：" + normalized);
    }
}
