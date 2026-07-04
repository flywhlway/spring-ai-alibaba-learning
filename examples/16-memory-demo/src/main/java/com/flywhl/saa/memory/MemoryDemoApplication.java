package com.flywhl.saa.memory;

import com.flywhl.saa.common.exception.GlobalExceptionHandler;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

/**
 * ChatMemory 新 API 演示应用：滑动窗口 + 会话隔离。
 *
 * @author flywhl
 */
@SpringBootApplication
@Import(GlobalExceptionHandler.class)
public class MemoryDemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(MemoryDemoApplication.class, args);
    }
}
