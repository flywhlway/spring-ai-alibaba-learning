package com.flywhl.saa.logging;

import com.flywhl.saa.common.exception.GlobalExceptionHandler;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

/**
 * MDC traceId + starter 审计日志贯通演示（对应教程第 18 章 Logging 段落）。
 *
 * @author flywhl
 */
@SpringBootApplication
@Import(GlobalExceptionHandler.class)
public class LoggingDemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(LoggingDemoApplication.class, args);
    }
}
