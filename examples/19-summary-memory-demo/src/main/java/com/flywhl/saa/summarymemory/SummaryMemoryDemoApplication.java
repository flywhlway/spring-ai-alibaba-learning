package com.flywhl.saa.summarymemory;

import com.flywhl.saa.common.exception.GlobalExceptionHandler;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

/**
 * 摘要压缩长对话演示（对应教程第 08 章 FAQ）。
 *
 * @author flywhl
 */
@SpringBootApplication
@Import(GlobalExceptionHandler.class)
public class SummaryMemoryDemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(SummaryMemoryDemoApplication.class, args);
    }
}
