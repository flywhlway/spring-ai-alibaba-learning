package com.flywhl.saa.mcpnacos.client;

import com.flywhl.saa.common.exception.GlobalExceptionHandler;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

/**
 * 办公助手 MCP Client：经 Nacos 按服务名发现 order-service-mcp。
 *
 * @author flywhl
 */
@SpringBootApplication
@Import(GlobalExceptionHandler.class)
public class OfficeAssistantClientApplication {

    public static void main(String[] args) {
        SpringApplication.run(OfficeAssistantClientApplication.class, args);
    }
}
