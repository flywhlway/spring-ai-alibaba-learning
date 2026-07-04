package com.flywhl.saa.workflow;

import com.flywhl.saa.common.exception.GlobalExceptionHandler;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

/**
 * 线性 StateGraph 工作流演示：rewrite → retrieve → generate。
 *
 * @author flywhl
 */
@SpringBootApplication
@Import(GlobalExceptionHandler.class)
public class WorkflowDemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(WorkflowDemoApplication.class, args);
    }
}
