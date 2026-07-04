package com.flywhl.saa.prompt;

import com.flywhl.saa.common.exception.GlobalExceptionHandler;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

/**
 * Prompt 工程范式演示应用：模板渲染 / Few-shot / CoT / JSON 格式化输出。
 *
 * @author flywhl
 */
@SpringBootApplication
@Import(GlobalExceptionHandler.class)
public class PromptDemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(PromptDemoApplication.class, args);
    }
}
