package com.flywhl.saa.mcpnacos.server;

import com.flywhl.saa.common.exception.GlobalExceptionHandler;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

/**
 * 订单 MCP Server：启动后自动注册到 Nacos MCP Registry。
 *
 * @author flywhl
 */
@SpringBootApplication
@Import(GlobalExceptionHandler.class)
public class OrderMcpServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(OrderMcpServerApplication.class, args);
    }
}
