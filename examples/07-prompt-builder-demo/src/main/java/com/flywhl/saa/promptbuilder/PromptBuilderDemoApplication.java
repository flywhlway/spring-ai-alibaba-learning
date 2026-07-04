package com.flywhl.saa.promptbuilder;

import com.flywhl.saa.common.exception.GlobalExceptionHandler;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

/**
 * Prompt 组装器与版本化管理演示应用。
 *
 * @author flywhl
 */
@SpringBootApplication
@Import(GlobalExceptionHandler.class)
public class PromptBuilderDemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(PromptBuilderDemoApplication.class, args);
    }
}
