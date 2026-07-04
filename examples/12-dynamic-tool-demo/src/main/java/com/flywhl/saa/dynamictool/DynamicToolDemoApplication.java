package com.flywhl.saa.dynamictool;

import com.flywhl.saa.common.exception.GlobalExceptionHandler;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

/**
 * 运行时动态注册 ToolCallback、异步 Tool 演示应用。
 *
 * @author flywhl
 */
@SpringBootApplication
@Import(GlobalExceptionHandler.class)
public class DynamicToolDemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(DynamicToolDemoApplication.class, args);
    }
}
