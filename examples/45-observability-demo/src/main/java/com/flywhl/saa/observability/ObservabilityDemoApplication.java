package com.flywhl.saa.observability;

import com.flywhl.saa.common.exception.GlobalExceptionHandler;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

/**
 * Micrometer + Prometheus 可观测演示（对应教程第 18 章）。
 *
 * @author flywhl
 */
@SpringBootApplication
@Import(GlobalExceptionHandler.class)
public class ObservabilityDemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(ObservabilityDemoApplication.class, args);
    }
}
