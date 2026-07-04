package com.flywhl.saa.httptool;

/**
 * 股票行情响应体。
 *
 * @param symbol   股票代码
 * @param price    最新价
 * @param currency 币种
 * @author flywhl
 */
public record StockPrice(String symbol, double price, String currency) {
}
