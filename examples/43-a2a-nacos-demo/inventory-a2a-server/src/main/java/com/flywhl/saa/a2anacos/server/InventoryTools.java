package com.flywhl.saa.a2anacos.server;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 库存查询 @Tool，供 inventory-agent 调用。
 *
 * @author flywhl
 */
@Component
public class InventoryTools {

    private static final Map<String, String> STOCK = Map.of(
            "SKU-001", "120 件（充足）",
            "SKU-002", "8 件（低库存，建议补货）",
            "SKU-003", "0 件（缺货）");

    @Tool(description = "按 SKU 查询商品库存数量与状态")
    public String queryStock(@ToolParam(description = "商品 SKU，如 SKU-001") String sku) {
        return STOCK.getOrDefault(sku.toUpperCase(), "未找到 SKU：" + sku);
    }
}
