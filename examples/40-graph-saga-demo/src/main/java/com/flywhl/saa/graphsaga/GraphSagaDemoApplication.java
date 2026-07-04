package com.flywhl.saa.graphsaga;

import com.flywhl.saa.common.exception.GlobalExceptionHandler;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

/**
 * 最小 Saga 补偿图演示：扣库存 → 扣款 → 失败则补偿库存。
 *
 * @author flywhl
 */
@SpringBootApplication
@Import(GlobalExceptionHandler.class)
public class GraphSagaDemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(GraphSagaDemoApplication.class, args);
    }
}
