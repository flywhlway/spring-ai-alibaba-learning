package com.flywhl.saa.agenthitl;

import com.flywhl.saa.common.exception.GlobalExceptionHandler;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

/**
 * Human-in-the-Loop 演示：高风险支付工具执行前暂停，人工确认后恢复。
 *
 * @author flywhl
 */
@SpringBootApplication
@Import(GlobalExceptionHandler.class)
public class AgentHitlDemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(AgentHitlDemoApplication.class, args);
    }
}
