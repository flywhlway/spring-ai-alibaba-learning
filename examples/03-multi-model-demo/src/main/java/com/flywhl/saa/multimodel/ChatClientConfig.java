package com.flywhl.saa.multimodel;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 当 classpath 同时存在 DashScope 与 DeepSeek 两个 Starter 时，
 * Spring 容器中会同时出现两个 {@link ChatModel} 实现 Bean，
 * 官方自动装配的 {@code ChatClient.Builder} 无法判断该基于哪一个构建——
 * 因此显式声明两个具名 {@link ChatClient} Bean，替代对自动装配 Builder 的依赖。
 *
 * @author flywhl
 */
@Configuration(proxyBeanMethods = false)
public class ChatClientConfig {

    /**
     * DashScope 通道：中文语境 / 日常问答场景默认首选。
     */
    @Bean
    public ChatClient dashScopeChatClient(ChatModel dashScopeChatModel) {
        return ChatClient.builder(dashScopeChatModel)
                .defaultSystem("你是通义千问驱动的助手，回答简洁准确")
                .defaultAdvisors(new SimpleLoggerAdvisor())
                .build();
    }

    /**
     * DeepSeek 通道：推理/代码类任务的备选与对比通道。
     */
    @Bean
    public ChatClient deepSeekChatClient(ChatModel deepSeekChatModel) {
        return ChatClient.builder(deepSeekChatModel)
                .defaultSystem("你是 DeepSeek 驱动的助手，回答简洁准确")
                .defaultAdvisors(new SimpleLoggerAdvisor())
                .build();
    }
}
