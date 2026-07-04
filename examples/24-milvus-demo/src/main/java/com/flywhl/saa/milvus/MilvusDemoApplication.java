package com.flywhl.saa.milvus;

import com.flywhl.saa.common.exception.GlobalExceptionHandler;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

/**
 * Milvus VectorStore 演示应用。
 *
 * @author flywhl
 */
@SpringBootApplication
@Import(GlobalExceptionHandler.class)
public class MilvusDemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(MilvusDemoApplication.class, args);
    }
}
