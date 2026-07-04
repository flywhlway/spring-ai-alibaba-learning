package com.flywhl.saa.redisvector;

import com.flywhl.saa.common.exception.GlobalExceptionHandler;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

/**
 * Redis Stack VectorStore 演示应用。
 *
 * @author flywhl
 */
@SpringBootApplication
@Import(GlobalExceptionHandler.class)
public class RedisVectorDemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(RedisVectorDemoApplication.class, args);
    }
}
