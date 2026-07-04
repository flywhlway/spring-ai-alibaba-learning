package com.flywhl.saa.prompt;

import com.flywhl.saa.common.result.Result;
import com.flywhl.saa.prompt.model.FaultDiagnosis;
import com.flywhl.saa.prompt.model.TemplateRequest;
import jakarta.validation.Valid;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * 四种经典 Prompt 范式演示：模板渲染 / Few-shot / CoT / JSON 格式化输出（对应教程第 05 章 §5.1-5.2）。
 *
 * @author flywhl
 */
@RestController
public class PromptController {

    private final ChatClient chatClient;

    public PromptController(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder.build();
    }

    /**
     * {@code PromptTemplate} 变量渲染：{@code {}} 占位符（StringTemplate 引擎，§5.1）。
     */
    @PostMapping("/prompt/template")
    public Result<String> template(@Valid @RequestBody TemplateRequest request) {
        PromptTemplate promptTemplate = PromptTemplate.builder()
                .template("请分析以下{domain}领域的日志，给出根因猜测：\n{log}")
                .build();
        String rendered = promptTemplate.render(Map.of("domain", request.domain(), "log", request.log()));
        return Result.ok(chatClient.prompt().user(rendered).call().content());
    }

    /**
     * Few-shot：用示例 User/Assistant 消息对引导模型模仿输出风格（§5.2）。
     */
    @GetMapping("/prompt/few-shot")
    public Result<String> fewShot(@RequestParam(defaultValue = "P0420") String dtcCode) {
        List<Message> fewShotMessages = List.of(
                new SystemMessage("你是故障码翻译助手，将 DTC 故障码翻译为中文描述"),
                new UserMessage("P0300"),
                new AssistantMessage("随机/多缸失火"),
                new UserMessage("P0171"),
                new AssistantMessage("系统偏稀（1号库）"),
                new UserMessage(dtcCode));
        String result = chatClient.prompt(new Prompt(fewShotMessages)).call().content();
        return Result.ok(result);
    }

    /**
     * CoT：System Prompt 引导模型分步推理后再给出结论（§5.2）。
     */
    @GetMapping("/prompt/cot")
    public Result<String> cot(@RequestParam(defaultValue = "车辆频繁出现 P0420 故障码，可能是什么原因？") String question) {
        String cotSystem = """
                你是资深故障诊断专家。请按以下步骤思考后再给出结论：
                1. 列出可能原因（至少3个）
                2. 逐一分析每个原因的可能性
                3. 给出最终诊断结论
                """;
        String result = chatClient.prompt().system(cotSystem).user(question).call().content();
        return Result.ok(result);
    }

    /**
     * JSON 格式化输出：{@code BeanOutputConverter} 生成格式约束指令并解析模型输出为强类型对象。
     */
    @GetMapping("/prompt/json")
    public Result<FaultDiagnosis> json(@RequestParam(defaultValue = "P0420") String dtcCode) {
        BeanOutputConverter<FaultDiagnosis> converter = new BeanOutputConverter<>(FaultDiagnosis.class);
        PromptTemplate promptTemplate = PromptTemplate.builder()
                .template("请分析故障码 {code} 的可能原因、严重程度与建议处理动作。\n{format}")
                .build();
        String rendered = promptTemplate.render(Map.of("code", dtcCode, "format", converter.getFormat()));
        String content = chatClient.prompt().user(rendered).call().content();
        return Result.ok(converter.convert(content));
    }
}
