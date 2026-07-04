package com.flywhl.saa.autoconfig;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 演示 {@code @ConfigurationProperties} 属性绑定，
 * 对应 DashScope 装配链路中 {@code DashScopeChatProperties} 的简化版原型。
 *
 * @param prefix  问候语前缀，默认 "Hello"
 * @param locale  语言环境，默认 "zh-CN"
 * @param enabled 功能开关，默认 true
 * @author flywhl
 */
@ConfigurationProperties(prefix = "saa.demo.greeting")
public record GreetingProperties(String prefix, String locale, boolean enabled) {

    public GreetingProperties {
        if (prefix == null || prefix.isBlank()) {
            prefix = "Hello";
        }
        if (locale == null || locale.isBlank()) {
            locale = "zh-CN";
        }
    }
}
