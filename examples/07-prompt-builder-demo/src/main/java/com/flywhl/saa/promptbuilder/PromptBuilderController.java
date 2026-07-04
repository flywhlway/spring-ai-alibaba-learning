package com.flywhl.saa.promptbuilder;

import com.flywhl.saa.common.result.Result;
import com.flywhl.saa.promptbuilder.model.PromptVersionVO;
import com.flywhl.saa.promptbuilder.model.RegisterPromptRequest;
import com.flywhl.saa.promptbuilder.model.RenderRequest;
import jakarta.validation.Valid;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Prompt 组装器 REST 入口：按 {@code name@version} 注册、查询、渲染、调用模型（对应教程第 05 章 §5.3）。
 *
 * @author flywhl
 */
@RestController
@RequestMapping("/prompts")
public class PromptBuilderController {

    private final PromptRegistry registry;
    private final ChatClient chatClient;

    public PromptBuilderController(PromptRegistry registry, ChatClient.Builder chatClientBuilder) {
        this.registry = registry;
        this.chatClient = chatClientBuilder.build();
    }

    @PostMapping
    public Result<Void> register(@Valid @RequestBody RegisterPromptRequest request) {
        registry.register(request.name(), request.version(), request.template());
        return Result.ok();
    }

    @GetMapping("/{name}")
    public Result<List<String>> listVersions(@PathVariable String name) {
        return Result.ok(registry.listVersions(name));
    }

    @GetMapping("/{name}/{version}")
    public Result<PromptVersionVO> get(@PathVariable String name, @PathVariable String version) {
        return Result.ok(registry.get(name, version));
    }

    @PostMapping("/{name}/{version}/render")
    public Result<String> render(@PathVariable String name, @PathVariable String version,
                                  @RequestBody RenderRequest request) {
        return Result.ok(registry.render(name, version, request.params()));
    }

    @PostMapping("/{name}/{version}/invoke")
    public Result<String> invoke(@PathVariable String name, @PathVariable String version,
                                  @RequestBody RenderRequest request) {
        String rendered = registry.render(name, version, request.params());
        return Result.ok(chatClient.prompt().user(rendered).call().content());
    }
}
