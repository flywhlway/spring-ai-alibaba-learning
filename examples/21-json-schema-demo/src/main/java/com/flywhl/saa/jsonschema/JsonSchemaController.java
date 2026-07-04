package com.flywhl.saa.jsonschema;

import com.flywhl.saa.common.exception.BizException;
import com.flywhl.saa.common.result.CommonResultCode;
import com.flywhl.saa.common.result.Result;
import com.flywhl.saa.jsonschema.model.ActorFilms;
import com.flywhl.saa.jsonschema.model.InspectionReport;
import com.flywhl.saa.jsonschema.model.ResilientReportVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.StructuredOutputValidationAdvisor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 嵌套泛型结构化输出与 Schema 校验容错。
 *
 * <p>禁止 {@code .entity(List.class)}——泛型擦除会丢失元素类型，必须用
 * {@link ParameterizedTypeReference}。
 *
 * @author flywhl
 */
@RestController
public class JsonSchemaController {

    private static final Logger log = LoggerFactory.getLogger(JsonSchemaController.class);

    private static final ParameterizedTypeReference<List<ActorFilms>> ACTOR_FILMS_LIST =
            new ParameterizedTypeReference<>() {};

    private final ChatClient chatClient;

    public JsonSchemaController(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder.build();
    }

    /**
     * 泛型集合：{@code ParameterizedTypeReference<List<ActorFilms>>} + validateSchema 语义。
     */
    @GetMapping("/filmography")
    public Result<List<ActorFilms>> filmography(
            @RequestParam(defaultValue = "汤姆·汉克斯,比尔·默瑞") String actors) {
        // validateSchema：校验失败自动重试（禁止 .entity(List.class)）
        StructuredOutputValidationAdvisor validateSchema = StructuredOutputValidationAdvisor.builder()
                .outputType(ACTOR_FILMS_LIST)
                .maxRepeatAttempts(3)
                .build();

        List<ActorFilms> films = chatClient.prompt()
                .advisors(validateSchema)
                .user("生成以下演员各 3 部代表电影的 filmography，演员列表：%s".formatted(actors))
                .call()
                .entity(ACTOR_FILMS_LIST);
        return Result.ok(films);
    }

    /**
     * 嵌套 Record：{@link InspectionReport} 内含 {@code List<InspectionFinding>}。
     */
    @GetMapping("/report/nested")
    public Result<InspectionReport> nestedReport(
            @RequestParam(defaultValue = "电动汽车电池热管理") String topic) {
        StructuredOutputValidationAdvisor validateSchema = StructuredOutputValidationAdvisor.builder()
                .outputType(InspectionReport.class)
                .maxRepeatAttempts(3)
                .build();

        InspectionReport report = chatClient.prompt()
                .advisors(validateSchema)
                .user("针对主题「%s」生成一份简短检查报告，包含总体结论和 2~3 条发现（含严重程度）"
                        .formatted(topic))
                .call()
                .entity(InspectionReport.class);
        return Result.ok(report);
    }

    /**
     * 校验重试耗尽后的应用层容错：先走 validateSchema，失败则宽松 {@code .entity()} 回退。
     */
    @GetMapping("/report/resilient")
    public Result<ResilientReportVO> resilientReport(
            @RequestParam(defaultValue = "数据中心机房巡检") String topic) {
        String userPrompt = "针对主题「%s」生成一份简短检查报告，包含总体结论和 2~3 条发现（含严重程度）"
                .formatted(topic);

        StructuredOutputValidationAdvisor validateSchema = StructuredOutputValidationAdvisor.builder()
                .outputType(InspectionReport.class)
                .maxRepeatAttempts(2)
                .build();

        try {
            InspectionReport report = chatClient.prompt()
                    .advisors(validateSchema)
                    .user(userPrompt)
                    .call()
                    .entity(InspectionReport.class);
            return Result.ok(new ResilientReportVO(report, false, "validateSchema"));
        } catch (Exception validationEx) {
            log.warn("validateSchema 重试耗尽，进入宽松解析回退: {}", validationEx.getMessage());
            try {
                InspectionReport fallback = chatClient.prompt()
                        .user(userPrompt)
                        .call()
                        .entity(InspectionReport.class);
                return Result.ok(new ResilientReportVO(fallback, true, "fallback-entity"));
            } catch (Exception fallbackEx) {
                throw new BizException(
                        CommonResultCode.INTERNAL_ERROR,
                        "结构化输出校验失败且容错解析亦失败: " + fallbackEx.getMessage(),
                        fallbackEx);
            }
        }
    }
}
