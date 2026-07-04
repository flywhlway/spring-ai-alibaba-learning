package com.flywhl.saa.advisor;

import com.flywhl.saa.common.exception.GlobalExceptionHandler;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

/**
 * 内置 Advisor 链顺序可视化演示（对应教程第 06 章）。
 *
 * @author flywhl
 */
@SpringBootApplication
@Import(GlobalExceptionHandler.class)
public class AdvisorDemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(AdvisorDemoApplication.class, args);
    }
}
