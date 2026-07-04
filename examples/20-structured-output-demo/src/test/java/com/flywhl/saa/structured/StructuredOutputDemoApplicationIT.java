package com.flywhl.saa.structured;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.StructuredOutputValidationAdvisor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 冒烟 IT：有 DashScope Key 时验证 .entity(Record) + validateSchema 产出非空字段。
 *
 * @author flywhl
 */
@SpringBootTest
@EnabledIfEnvironmentVariable(named = "AI_DASHSCOPE_API_KEY", matches = ".+")
class StructuredOutputDemoApplicationIT {

    @Autowired
    private ChatClient.Builder chatClientBuilder;

    @Test
    void diagnoseEntityHasNonEmptyFields() {
        StructuredOutputValidationAdvisor validateSchema = StructuredOutputValidationAdvisor.builder()
                .outputType(DiagnosisResult.class)
                .maxRepeatAttempts(3)
                .build();

        DiagnosisResult result = chatClientBuilder.build().prompt()
                .advisors(validateSchema)
                .user("分析故障码P0420的根本原因，给出0-100的置信度评分和建议措施")
                .call()
                .entity(DiagnosisResult.class);

        assertThat(result).isNotNull();
        assertThat(result.rootCause()).isNotBlank();
        assertThat(result.confidenceScore()).isBetween(0, 100);
        assertThat(result.suggestedActions()).isNotEmpty();
        assertThat(result.suggestedActions()).allSatisfy(action -> assertThat(action).isNotBlank());
    }
}
