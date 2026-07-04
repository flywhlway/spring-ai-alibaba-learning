package com.flywhl.saa.embedding;

import com.flywhl.saa.common.exception.GlobalExceptionHandler;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

/**
 * Embedding 维度与成本基准测试演示应用。
 *
 * @author flywhl
 */
@SpringBootApplication
@Import(GlobalExceptionHandler.class)
public class EmbeddingDemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(EmbeddingDemoApplication.class, args);
    }
}
