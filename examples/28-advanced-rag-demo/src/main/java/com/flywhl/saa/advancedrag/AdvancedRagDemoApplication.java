package com.flywhl.saa.advancedrag;

import com.flywhl.saa.common.exception.GlobalExceptionHandler;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

/**
 * Advanced RAG 演示：查询改写 + 向量检索 + 分数重排序。
 *
 * @author flywhl
 */
@SpringBootApplication
@Import(GlobalExceptionHandler.class)
public class AdvancedRagDemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(AdvancedRagDemoApplication.class, args);
    }
}
