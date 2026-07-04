package com.flywhl.saa.quickstart;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 最小可运行 SAA 应用：验证 JDK 21 / DashScope Key / 网络配置是否正确。
 *
 * @author flywhl
 */
@SpringBootApplication
public class QuickstartApplication {

    public static void main(String[] args) {
        SpringApplication.run(QuickstartApplication.class, args);
    }
}
