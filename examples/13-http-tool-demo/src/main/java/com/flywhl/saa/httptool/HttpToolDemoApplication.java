package com.flywhl.saa.httptool;

import com.flywhl.saa.common.exception.GlobalExceptionHandler;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

/**
 * HTTP API 封装为 Tool 演示应用（对应教程第 07 章 §7.6）。
 *
 * @author flywhl
 */
@SpringBootApplication
@Import(GlobalExceptionHandler.class)
public class HttpToolDemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(HttpToolDemoApplication.class, args);
    }
}
