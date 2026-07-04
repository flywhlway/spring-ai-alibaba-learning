package com.flywhl.saa.structured;

import com.flywhl.saa.common.exception.GlobalExceptionHandler;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

/**
 * Structured Output 演示：Record + validateSchema 自动校验重试。
 *
 * @author flywhl
 */
@SpringBootApplication
@Import(GlobalExceptionHandler.class)
public class StructuredOutputDemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(StructuredOutputDemoApplication.class, args);
    }
}
