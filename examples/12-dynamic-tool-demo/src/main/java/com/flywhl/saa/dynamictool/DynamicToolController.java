package com.flywhl.saa.dynamictool;

import com.flywhl.saa.common.result.Result;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author flywhl
 */
@RestController
public class DynamicToolController {

    private final ChatClient chatClient;
    private final DynamicToolFactory dynamicToolFactory;

    public DynamicToolController(ChatClient.Builder chatClientBuilder,
                                  AsyncTools asyncTools,
                                  DynamicToolFactory dynamicToolFactory) {
        // 异步库存工具全局常驻；计算器工具不在这里注册，改为按请求动态挑选
        this.chatClient = chatClientBuilder.defaultTools(asyncTools).build();
        this.dynamicToolFactory = dynamicToolFactory;
    }

    @GetMapping("/tool/dynamic")
    public Result<String> dynamic(@RequestParam String question,
                                   @RequestParam(defaultValue = "false") boolean enableCalculator) {
        ChatClient.ChatClientRequestSpec spec = chatClient.prompt().user(question);
        if (enableCalculator) {
            spec = spec.toolCallbacks(dynamicToolFactory.calculatorTool());
        }
        return Result.ok(spec.call().content());
    }
}
