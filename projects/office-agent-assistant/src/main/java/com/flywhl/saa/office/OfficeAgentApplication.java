package com.flywhl.saa.office;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

/**
 * 项目二 · 企业 AI Agent 办公助手 启动入口（端口 19200）。
 *
 * @author flywhl
 */
@SpringBootApplication
@ConfigurationPropertiesScan
public class OfficeAgentApplication {

    public static void main(String[] args) {
        SpringApplication.run(OfficeAgentApplication.class, args);
    }
}
