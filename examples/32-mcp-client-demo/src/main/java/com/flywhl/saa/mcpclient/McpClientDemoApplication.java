package com.flywhl.saa.mcpclient;

import com.flywhl.saa.common.exception.GlobalExceptionHandler;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

/**
 * MCP Client 演示：通过 Streamable HTTP 消费本机 31-mcp-server-demo。
 *
 * @author flywhl
 */
@SpringBootApplication
@Import(GlobalExceptionHandler.class)
public class McpClientDemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(McpClientDemoApplication.class, args);
    }
}
