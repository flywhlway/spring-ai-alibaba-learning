package com.flywhl.saa.autoconfig;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 自动装配教学 Demo 启动类：不调用任何模型，纯粹演示自定义自动装配模块。
 *
 * @author flywhl
 */
@SpringBootApplication
public class AutoconfigDemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(AutoconfigDemoApplication.class, args);
    }
}
