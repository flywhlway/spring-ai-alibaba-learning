package com.flywhl.saa.mcpserver;

import com.flywhl.saa.common.exception.GlobalExceptionHandler;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

/**
 * MCP Server 演示：通过 Streamable HTTP 暴露 {@code @McpTool}。
 *
 * @author flywhl
 */
@SpringBootApplication
@Import(GlobalExceptionHandler.class)
public class McpServerDemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(McpServerDemoApplication.class, args);
    }
}
