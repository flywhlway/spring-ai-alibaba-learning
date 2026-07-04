package com.flywhl.saa.mcpauth;

import com.flywhl.saa.common.exception.GlobalExceptionHandler;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

/**
 * MCP Server 鉴权演示：Bearer Token 经传输层注入，工具内校验。
 *
 * @author flywhl
 */
@SpringBootApplication
@Import(GlobalExceptionHandler.class)
public class McpAuthDemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(McpAuthDemoApplication.class, args);
    }
}
