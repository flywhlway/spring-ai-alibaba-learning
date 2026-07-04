package com.flywhl.saa.supervisor;

import com.flywhl.saa.common.exception.GlobalExceptionHandler;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

/**
 * ReactAgent 总控 + AgentTool.create 子 Agent 办公助手演示。
 *
 * @author flywhl
 */
@SpringBootApplication
@Import(GlobalExceptionHandler.class)
public class SupervisorDemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(SupervisorDemoApplication.class, args);
    }
}
