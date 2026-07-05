package com.flywhl.saa.knowledgeqa;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

/**
 * 项目一 · AI 企业知识库问答平台 启动入口（端口 19100）。
 *
 * <p>业务蓝图 SSOT 见 {@code projects/README.md}「项目一」；模块分层：
 * controller / service / rag / prompt / tool / admin / model / mapper / repository / config。
 *
 * @author flywhl
 * @since 1.0.0
 */
@SpringBootApplication
@ConfigurationPropertiesScan
public class KnowledgeQaApplication {

    public static void main(String[] args) {
        SpringApplication.run(KnowledgeQaApplication.class, args);
    }
}
