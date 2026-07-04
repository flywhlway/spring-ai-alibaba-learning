package com.flywhl.saa.hybridrag;

import com.flywhl.saa.common.exception.GlobalExceptionHandler;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

/**
 * Hybrid RAG + Citation 演示应用。
 *
 * @author flywhl
 */
@SpringBootApplication
@Import(GlobalExceptionHandler.class)
public class HybridRagDemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(HybridRagDemoApplication.class, args);
    }
}
