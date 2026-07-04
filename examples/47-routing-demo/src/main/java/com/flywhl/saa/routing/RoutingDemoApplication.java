package com.flywhl.saa.routing;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 多模型路由 Demo：复用 starter {@link com.flywhl.saa.starter.routing.ModelRouter}。
 *
 * @author flywhl
 */
@SpringBootApplication
public class RoutingDemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(RoutingDemoApplication.class, args);
    }
}
