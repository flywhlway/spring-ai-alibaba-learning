package com.flywhl.saa.knowledgeqa.config;

import java.util.concurrent.Executor;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

/**
 * 异步执行器配置：文档解析-向量化流水线专用线程池（隔离于 Web 线程）。
 *
 * @author flywhl
 * @since 1.0.0
 */
@Configuration
@EnableAsync
public class AsyncConfig {

    @Bean(name = "etlExecutor")
    Executor etlExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(4);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("kqa-etl-");
        executor.initialize();
        return executor;
    }
}
