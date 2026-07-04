package com.flywhl.saa.httptool;

import com.flywhl.saa.common.result.Result;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * HTTP Tool 问答入口。
 *
 * @author flywhl
 */
@RestController
public class HttpToolController {

    private final ChatClient chatClient;

    public HttpToolController(ChatClient.Builder chatClientBuilder, StockPriceTools tools) {
        this.chatClient = chatClientBuilder.defaultTools(tools).build();
    }

    @GetMapping("/ask")
    public Result<String> ask(@RequestParam String question) {
        return Result.ok(chatClient.prompt().user(question).call().content());
    }
}
