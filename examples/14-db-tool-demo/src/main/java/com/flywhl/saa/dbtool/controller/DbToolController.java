package com.flywhl.saa.dbtool.controller;

import com.flywhl.saa.common.result.Result;
import com.flywhl.saa.dbtool.model.Product;
import com.flywhl.saa.dbtool.tool.ProductTools;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 数据库工具 Demo：一条自然语言问题触发模型选择 {@link ProductTools} 中的
 * 参数化查询方法；另附两个直连接口，便于在无模型 Key 的环境下单独验证
 * SQL 查询本身的正确性与安全性。
 *
 * @author flywhl
 */
@RestController
public class DbToolController {

    private final ChatClient chatClient;
    private final ProductTools productTools;

    public DbToolController(ChatClient.Builder chatClientBuilder, ProductTools productTools) {
        this.productTools = productTools;
        this.chatClient = chatClientBuilder
                .defaultSystem("你是商品查询助手，遇到商品相关问题时调用工具查询真实数据，不要凭空编造")
                .defaultTools(productTools)
                .build();
    }

    /**
     * 模型驱动查询：模型决定调用 {@code searchProductsByCategory} 还是 {@code getProductById}。
     */
    @GetMapping("/db/ask")
    public Result<String> ask(@RequestParam String question) {
        String content = chatClient.prompt().user(question).call().content();
        return Result.ok(content);
    }

    /**
     * 直连接口：跳过模型，直接验证参数化查询本身（无需 {@code AI_DASHSCOPE_API_KEY}）。
     */
    @GetMapping("/db/products")
    public Result<?> byCategory(@RequestParam String category) {
        return Result.ok(productTools.searchProductsByCategory(category));
    }

    @GetMapping("/db/products/{id}")
    public Result<Product> byId(@PathVariable Long id) {
        return Result.ok(productTools.getProductById(id));
    }
}
