package com.flywhl.saa.dbtool.tool;

import com.flywhl.saa.common.exception.BizException;
import com.flywhl.saa.common.result.CommonResultCode;
import com.flywhl.saa.dbtool.model.Product;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 数据库查询工具：全部使用 {@link JdbcTemplate} 的 {@code ?} 占位符参数化查询，
 * 用户/模型提供的入参永远作为绑定参数传入，绝不拼接进 SQL 字符串——
 * 这是防止 SQL 注入的唯一正确姿势（第 07 章 §7.7 / 安全建议）。
 *
 * @author flywhl
 */
@Component
public class ProductTools {

    private static final RowMapper<Product> PRODUCT_ROW_MAPPER = (rs, rowNum) -> new Product(
            rs.getLong("id"),
            rs.getString("name"),
            rs.getString("category"),
            rs.getBigDecimal("price"),
            rs.getInt("stock"));

    private final JdbcTemplate jdbcTemplate;

    public ProductTools(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Tool(description = "按分类查询商品列表，分类如：笔记本电脑、手机、耳机")
    public List<Product> searchProductsByCategory(@ToolParam(description = "商品分类") String category) {
        // 参数化查询：category 作为绑定参数传入，而非拼接进 SQL 文本
        return jdbcTemplate.query(
                "SELECT id, name, category, price, stock FROM products WHERE category = ?",
                PRODUCT_ROW_MAPPER, category);
    }

    @Tool(description = "根据商品 ID 查询商品详情")
    public Product getProductById(@ToolParam(description = "商品 ID") Long id) {
        try {
            return jdbcTemplate.queryForObject(
                    "SELECT id, name, category, price, stock FROM products WHERE id = ?",
                    PRODUCT_ROW_MAPPER, id);
        } catch (EmptyResultDataAccessException e) {
            throw new BizException(CommonResultCode.NOT_FOUND, "商品不存在：id=" + id);
        }
    }
}
