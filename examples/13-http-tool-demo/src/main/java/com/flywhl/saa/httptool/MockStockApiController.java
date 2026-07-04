package com.flywhl.saa.httptool;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 模拟外部行情 HTTP API，供 {@link StockPriceTools} 通过 RestClient 调用。
 *
 * <p>真实场景中 baseUrl 指向第三方服务；本 Demo 自包含，避免依赖外网。
 *
 * @author flywhl
 */
@RestController
public class MockStockApiController {

    @GetMapping("/mock/quote")
    public StockPrice quote(@RequestParam String symbol) {
        double price = 100.0 + Math.floorMod(symbol.toUpperCase().hashCode(), 500);
        return new StockPrice(symbol.toUpperCase(), price, "USD");
    }
}
