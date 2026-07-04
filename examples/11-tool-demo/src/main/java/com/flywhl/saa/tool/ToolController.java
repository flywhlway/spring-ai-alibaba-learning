package com.flywhl.saa.tool;

import com.flywhl.saa.common.result.Result;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * @author flywhl
 */
@RestController
public class ToolController {

    private final ChatClient chatClient;

    public ToolController(ChatClient.Builder chatClientBuilder, MemberTools memberTools) {
        this.chatClient = chatClientBuilder.defaultTools(memberTools).build();
    }

    /**
     * ToolContext 演示：userId 由服务端注入，模型只看到自然语言问题。
     */
    @GetMapping("/tool/context")
    public Result<String> context(@RequestParam String question,
                                   @RequestParam(defaultValue = "u-1001") String userId) {
        String content = chatClient.prompt()
                .toolContext(Map.of("userId", userId))
                .user(question)
                .call()
                .content();
        return Result.ok(content);
    }

    /**
     * returnDirect 演示：getServerTime 的返回值直接作为最终响应，不经模型转述。
     */
    @GetMapping("/tool/direct")
    public Result<String> direct(@RequestParam String city) {
        String content = chatClient.prompt()
                .user("查询" + city + "当前的服务器标准时间")
                .call()
                .content();
        return Result.ok(content);
    }
}
