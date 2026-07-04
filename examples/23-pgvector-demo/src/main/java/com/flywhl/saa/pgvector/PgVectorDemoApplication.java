package com.flywhl.saa.pgvector;

import com.flywhl.saa.common.exception.GlobalExceptionHandler;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

/**
 * PGVector VectorStore 演示应用。
 *
 * @author flywhl
 */
@SpringBootApplication
@Import(GlobalExceptionHandler.class)
public class PgVectorDemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(PgVectorDemoApplication.class, args);
    }
}
