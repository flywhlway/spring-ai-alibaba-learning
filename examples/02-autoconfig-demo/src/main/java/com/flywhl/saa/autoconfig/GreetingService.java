package com.flywhl.saa.autoconfig;

/**
 * 待自动装配的业务组件，对应链路中 {@code DashScopeChatModel} 的简化版原型。
 *
 * @author flywhl
 */
public class GreetingService {

    private final GreetingProperties properties;

    public GreetingService(GreetingProperties properties) {
        this.properties = properties;
    }

    public String greet(String name) {
        return "%s, %s! (locale=%s)".formatted(properties.prefix(), name, properties.locale());
    }
}
