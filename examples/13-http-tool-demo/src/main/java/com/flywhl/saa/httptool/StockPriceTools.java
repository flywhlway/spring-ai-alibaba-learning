package com.flywhl.saa.httptool;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

/**
 * 将外部 HTTP 行情 API 封装为 {@code @Tool}（教程 §7.6）。
 *
 * @author flywhl
 */
@Component
public class StockPriceTools {

    private final RestClient restClient;

    public StockPriceTools(@Value("${httptool.quote-base-url}") String baseUrl,
                           RestClient.Builder builder) {
        this.restClient = builder.baseUrl(baseUrl).build();
    }

    @Tool(description = "查询指定股票代码的最新价格")
    public StockPrice getLatestPrice(@ToolParam(description = "股票代码，如 AAPL") String symbol) {
        return restClient.get()
                .uri("/mock/quote?symbol={symbol}", symbol)
                .retrieve()
                .body(StockPrice.class);
    }
}
