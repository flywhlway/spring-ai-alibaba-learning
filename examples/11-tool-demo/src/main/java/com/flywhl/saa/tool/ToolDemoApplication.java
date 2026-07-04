package com.flywhl.saa.tool;

import com.flywhl.saa.common.exception.GlobalExceptionHandler;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

/**
 * Tool Calling 基础用法演示应用：@Tool 定义、ToolContext、returnDirect。
 *
 * @author flywhl
 */
@SpringBootApplication
@Import(GlobalExceptionHandler.class)
public class ToolDemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(ToolDemoApplication.class, args);
    }
}
