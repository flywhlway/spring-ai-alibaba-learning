package com.flywhl.saa.jdbcmemory;

import com.flywhl.saa.common.exception.GlobalExceptionHandler;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

/**
 * JDBC 持久化会话记忆演示（对应教程第 08 章可运行 Demo）。
 *
 * @author flywhl
 */
@SpringBootApplication
@Import(GlobalExceptionHandler.class)
public class JdbcMemoryDemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(JdbcMemoryDemoApplication.class, args);
    }
}
