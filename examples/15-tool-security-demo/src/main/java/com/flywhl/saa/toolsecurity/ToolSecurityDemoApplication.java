package com.flywhl.saa.toolsecurity;

import com.flywhl.saa.common.exception.GlobalExceptionHandler;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

/**
 * Tool 权限校验与调用审计演示应用。
 *
 * @author flywhl
 */
@SpringBootApplication
@Import(GlobalExceptionHandler.class)
public class ToolSecurityDemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(ToolSecurityDemoApplication.class, args);
    }
}
