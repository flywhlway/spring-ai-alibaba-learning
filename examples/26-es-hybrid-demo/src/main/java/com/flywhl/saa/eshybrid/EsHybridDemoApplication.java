package com.flywhl.saa.eshybrid;

import com.flywhl.saa.common.exception.GlobalExceptionHandler;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

/**
 * Elasticsearch 向量 + 全文混合检索演示应用。
 *
 * @author flywhl
 */
@SpringBootApplication
@Import(GlobalExceptionHandler.class)
public class EsHybridDemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(EsHybridDemoApplication.class, args);
    }
}
