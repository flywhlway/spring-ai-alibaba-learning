package com.flywhl.saa.rageval;

import com.flywhl.saa.common.exception.GlobalExceptionHandler;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

/**
 * RAG 评测演示：忠实度 / 相关性最小实现。
 *
 * @author flywhl
 */
@SpringBootApplication
@Import(GlobalExceptionHandler.class)
public class RagEvalDemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(RagEvalDemoApplication.class, args);
    }
}
