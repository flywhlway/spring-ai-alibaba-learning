package com.flywhl.saa.structured;

import com.flywhl.saa.common.result.Result;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.StructuredOutputValidationAdvisor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 诊断结果结构化输出：{@code .entity(Record)} + Schema 校验自动重试。
 *
 * <p>教程写法 {@code .entity(T.class, spec -> spec.validateSchema())} 在 Spring AI 1.1.2
 * 中对应 {@link StructuredOutputValidationAdvisor}（同语义：校验失败追加错误并重试）。
 *
 * @author flywhl
 */
@RestController
public class StructuredOutputController {

    private final ChatClient chatClient;

    public StructuredOutputController(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder.build();
    }

    /**
     * 将故障码分析为强类型 {@link DiagnosisResult}；Schema 校验失败时框架自动重试。
     */
    @GetMapping("/diagnose/structured")
    public Result<DiagnosisResult> diagnose(@RequestParam String dtcCode) {
        // validateSchema 语义：StructuredOutputValidationAdvisor 校验失败后最多重试 maxRepeatAttempts 次
        StructuredOutputValidationAdvisor validateSchema = StructuredOutputValidationAdvisor.builder()
                .outputType(DiagnosisResult.class)
                .maxRepeatAttempts(3)
                .build();

        DiagnosisResult result = chatClient.prompt()
                .advisors(validateSchema)
                .user("分析故障码%s的根本原因，给出0-100的置信度评分和建议措施".formatted(dtcCode))
                .call()
                .entity(DiagnosisResult.class);
        return Result.ok(result);
    }
}
