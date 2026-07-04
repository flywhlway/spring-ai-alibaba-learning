package com.flywhl.saa.promptnacos;

import com.alibaba.cloud.ai.prompt.ConfigurablePromptTemplate;
import com.alibaba.cloud.ai.prompt.ConfigurablePromptTemplateFactory;
import com.flywhl.saa.common.result.Result;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 演示 Nacos 动态 Prompt：修改 Nacos 上 dataId={@code spring.ai.alibaba.configurable.prompt}
 * 的配置后，无需重启应用即可看到 Prompt 内容变化。
 *
 * <p>包名以 SAA 1.1.2.2 实际 API 为准：{@code com.alibaba.cloud.ai.prompt.*}
 * （教程正文中的 {@code autoconfigure.configurableprompt} 包名为历史写法）。
 *
 * @author flywhl
 */
@RestController
public class PromptNacosController {

    private final ChatClient chatClient;
    private final ConfigurablePromptTemplateFactory promptTemplateFactory;

    public PromptNacosController(ChatClient.Builder chatClientBuilder,
                                  ConfigurablePromptTemplateFactory promptTemplateFactory) {
        this.chatClient = chatClientBuilder.build();
        this.promptTemplateFactory = promptTemplateFactory;
    }

    @GetMapping("/diagnosis")
    public Result<String> diagnose(@RequestParam String code) {
        ConfigurablePromptTemplate template = promptTemplateFactory.getTemplate("dtc-diagnosis");
        if (template == null) {
            // Nacos 无配置或服务不可用时降级到代码内默认模板（企业实践强制要求）
            template = promptTemplateFactory.create(
                    "dtc-diagnosis",
                    "请分析故障码 {code} 的可能原因",
                    Map.of("code", "P0000"));
        }
        String prompt = template.render(Map.of("code", code));
        return Result.ok(chatClient.prompt().user(prompt).call().content());
    }
}
