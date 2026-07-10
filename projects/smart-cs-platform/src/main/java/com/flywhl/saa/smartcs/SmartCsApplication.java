package com.flywhl.saa.smartcs;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import com.flywhl.saa.smartcs.config.ScsProperties;

/**
 * 项目三 · 智能客服 Agent 平台 启动入口（端口 19300）。
 *
 * <p>业务蓝图 SSOT 见 {@code projects/README.md}「项目三」；模块分层：
 * controller / service / agent / tool / rag / prompt / admin / model / mapper / repository / config。
 *
 * @author flywhl
 * @since 1.0.0
 */
@SpringBootApplication
@EnableConfigurationProperties(ScsProperties.class)
@EnableJpaRepositories
@EnableTransactionManagement
public class SmartCsApplication {

    public static void main(String[] args) {
        SpringApplication.run(SmartCsApplication.class, args);
    }
}
