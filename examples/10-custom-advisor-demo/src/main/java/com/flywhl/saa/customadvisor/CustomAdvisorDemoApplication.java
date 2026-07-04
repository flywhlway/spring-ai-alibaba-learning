package com.flywhl.saa.customadvisor;

import com.flywhl.saa.common.exception.GlobalExceptionHandler;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

/**
 * 自定义审计 Advisor 演示应用（对应教程第 06 章 §6.6）。
 *
 * @author flywhl
 */
@SpringBootApplication
@Import(GlobalExceptionHandler.class)
public class CustomAdvisorDemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(CustomAdvisorDemoApplication.class, args);
    }
}
