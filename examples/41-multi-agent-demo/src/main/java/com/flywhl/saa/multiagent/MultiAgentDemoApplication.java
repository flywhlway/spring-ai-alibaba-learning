package com.flywhl.saa.multiagent;

import com.flywhl.saa.common.exception.GlobalExceptionHandler;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

/**
 * FlowAgent 四模式演示：Sequential / Parallel / LlmRouting / Loop。
 *
 * @author flywhl
 */
@SpringBootApplication
@Import(GlobalExceptionHandler.class)
public class MultiAgentDemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(MultiAgentDemoApplication.class, args);
    }
}
