package com.flywhl.saa.multimodel;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 显式路由到指定模型通道，并演示 Usage（Token 用量）读取。
 *
 * <p>构造函数参数名 {@code dashScopeChatClient} / {@code deepSeekChatClient} 分别匹配
 * {@link ChatClientConfig} 声明的具名 Bean——Spring 在多个同类型 Bean 时按参数名消歧，无需 {@code @Qualifier}。
 *
 * @author flywhl
 */
@RestController
public class MultiModelController {

    private final ChatClient dashScopeChatClient;
    private final ChatClient deepSeekChatClient;

    public MultiModelController(ChatClient dashScopeChatClient, ChatClient deepSeekChatClient) {
        this.dashScopeChatClient = dashScopeChatClient;
        this.deepSeekChatClient = deepSeekChatClient;
    }

    @GetMapping("/chat/dashscope")
    public String chatViaDashScope(@RequestParam String message) {
        return dashScopeChatClient.prompt().user(message).call().content();
    }

    @GetMapping("/chat/deepseek")
    public String chatViaDeepSeek(@RequestParam String message) {
        return deepSeekChatClient.prompt().user(message).call().content();
    }

    /** 演示 Usage 读取：返回文本 + 本次消耗 token 数 */
    @GetMapping("/chat/usage")
    public String chatWithUsage(@RequestParam String message) {
        var response = dashScopeChatClient.prompt().user(message).call().chatResponse();
        var usage = response.getMetadata().getUsage();
        return "回答：%s\n\n[token 用量] prompt=%d, completion=%d, total=%d".formatted(
                response.getResult().getOutput().getText(),
                usage.getPromptTokens(), usage.getCompletionTokens(), usage.getTotalTokens());
    }
}
