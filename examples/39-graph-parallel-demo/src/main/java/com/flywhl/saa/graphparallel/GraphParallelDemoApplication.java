package com.flywhl.saa.graphparallel;

import com.flywhl.saa.common.exception.GlobalExceptionHandler;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

/**
 * 并行 StateGraph 演示：知识库与历史工单 fan-out/fan-in。
 *
 * @author flywhl
 */
@SpringBootApplication
@Import(GlobalExceptionHandler.class)
public class GraphParallelDemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(GraphParallelDemoApplication.class, args);
    }
}
