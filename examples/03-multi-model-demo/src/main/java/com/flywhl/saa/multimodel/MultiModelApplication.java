package com.flywhl.saa.multimodel;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 多模型并存 Demo：DashScope 与 DeepSeek 两个 ChatModel 共存于同一应用。
 *
 * @author flywhl
 */
@SpringBootApplication
public class MultiModelApplication {

    public static void main(String[] args) {
        SpringApplication.run(MultiModelApplication.class, args);
    }
}
