package com.flywhl.saa.chat;

import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatOptions;
import com.flywhl.saa.chat.model.ChatRequest;
import com.flywhl.saa.chat.model.ChatVO;
import com.flywhl.saa.common.result.Result;
import jakarta.validation.Valid;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * ChatClient 全 API 演示：Fluent 链（system/user/options）、三种响应提取、Usage 读取。
 *
 * @author flywhl
 */
@RestController
public class ChatController {

    private final ChatClient chatClient;

    public ChatController(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder
                .defaultSystem("你是一个专业的技术助手，回答需严谨、有依据")
                .build();
    }

    /**
     * 最简用法：{@code .call().content()} 只取文本。
     */
    @GetMapping("/chat/simple")
    public Result<String> simple(@RequestParam(defaultValue = "用一句话介绍你自己") String message) {
        String content = chatClient.prompt().user(message).call().content();
        return Result.ok(content);
    }

    /**
     * 完整用法：系统提示覆盖 + 调用级温度覆盖 + 读取 {@code ChatResponse} 的 Usage/Metadata。
     */
    @PostMapping("/chat")
    public Result<ChatVO> chat(@Valid @RequestBody ChatRequest request) {
        ChatClient.ChatClientRequestSpec spec = chatClient.prompt().user(request.message());

        if (StringUtils.hasText(request.system())) {
            spec = spec.system(request.system());
        }
        if (request.temperature() != null) {
            // 调用级覆盖：DashScope Options 仍保留历史 withXxx 命名（见教程 §4.3）
            spec = spec.options(DashScopeChatOptions.builder()
                    .withTemperature(request.temperature())
                    .build());
        }

        ChatResponse response = spec.call().chatResponse();
        var usage = response.getMetadata().getUsage();
        ChatVO vo = new ChatVO(
                response.getResult().getOutput().getText(),
                response.getMetadata().getModel(),
                usage.getPromptTokens(),
                usage.getCompletionTokens(),
                usage.getTotalTokens());
        return Result.ok(vo);
    }
}
