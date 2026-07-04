package com.flywhl.saa.autoconfig;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 自定义自动装配类，完整复现 DashScope 装配链路的三个关键要素：
 * <ol>
 *   <li>{@link EnableConfigurationProperties} 启用属性绑定；</li>
 *   <li>{@link ConditionalOnProperty} 功能开关（对应 DashScope 的 api-key 判断）；</li>
 *   <li>{@link ConditionalOnMissingBean} 用户自定义优先（对应 ChatModel 的兜底策略）。</li>
 * </ol>
 *
 * @author flywhl
 */
@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(GreetingProperties.class)
@ConditionalOnProperty(prefix = "saa.demo.greeting", name = "enabled", havingValue = "true", matchIfMissing = true)
public class GreetingAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public GreetingService greetingService(GreetingProperties properties) {
        return new GreetingService(properties);
    }
}
