package com.flywhl.saa.a2anacos.server;

import com.flywhl.saa.common.exception.GlobalExceptionHandler;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

/**
 * 库存查询 A2A Server：ReactAgent 注册 AgentCard 到 Nacos。
 *
 * @author flywhl
 */
@SpringBootApplication
@Import(GlobalExceptionHandler.class)
public class InventoryA2aServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(InventoryA2aServerApplication.class, args);
    }
}
