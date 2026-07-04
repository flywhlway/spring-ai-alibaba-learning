package com.flywhl.saa.dbtool.model;

import java.math.BigDecimal;

/**
 * 商品实体（对应 {@code products} 表）。
 *
 * @author flywhl
 */
public record Product(Long id, String name, String category, BigDecimal price, int stock) {
}
