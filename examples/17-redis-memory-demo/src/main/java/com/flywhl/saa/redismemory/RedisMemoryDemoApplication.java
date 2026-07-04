package com.flywhl.saa.redismemory;

import com.flywhl.saa.common.exception.GlobalExceptionHandler;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

/**
 * Redis 持久化会话记忆演示应用（普通 Redis，非 Redis Stack）。
 *
 * @author flywhl
 */
@SpringBootApplication
@Import(GlobalExceptionHandler.class)
public class RedisMemoryDemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(RedisMemoryDemoApplication.class, args);
    }
}
