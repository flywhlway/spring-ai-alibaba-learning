package com.flywhl.saa.a2anacos.client;

import com.flywhl.saa.common.exception.GlobalExceptionHandler;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

/**
 * A2A Client：通过 Nacos 发现 inventory-agent 并远程调用。
 *
 * @author flywhl
 */
@SpringBootApplication
@Import(GlobalExceptionHandler.class)
public class OfficeA2aClientApplication {

    public static void main(String[] args) {
        SpringApplication.run(OfficeA2aClientApplication.class, args);
    }
}
