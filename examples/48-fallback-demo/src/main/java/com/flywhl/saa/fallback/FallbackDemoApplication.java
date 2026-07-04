package com.flywhl.saa.fallback;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 模型降级 Demo：复用 starter {@link com.flywhl.saa.starter.routing.FallbackModelRouter} 熔断语义。
 *
 * @author flywhl
 */
@SpringBootApplication
public class FallbackDemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(FallbackDemoApplication.class, args);
    }
}
