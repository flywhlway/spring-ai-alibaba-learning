package com.flywhl.saa.chat;

import com.flywhl.saa.common.exception.GlobalExceptionHandler;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

/**
 * ChatClient 全 API 演示应用。
 *
 * <p>通过 {@link Import} 引入仓库公共的 {@link GlobalExceptionHandler}，
 * 让本 Demo 复用统一的错误响应结构，而非重复实现异常处理。
 *
 * @author flywhl
 */
@SpringBootApplication
@Import(GlobalExceptionHandler.class)
public class ChatDemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(ChatDemoApplication.class, args);
    }
}
