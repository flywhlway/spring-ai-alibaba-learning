package com.flywhl.saa.smartcs;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;

import com.flywhl.saa.smartcs.prompt.PromptTemplateProvider;
import com.flywhl.saa.smartcs.support.ScsIntegrationTest;
import com.flywhl.saa.smartcs.support.ScsPostgresRedisITBase;

/**
 * 真机模型 IT（可选）：需 {@code AI_DASHSCOPE_API_KEY}；完整 FAQ ask 另需 Milvus/ES infra。
 * 无 Key 时自动 Disabled，不阻塞 CI。
 *
 * @author flywhl
 */
@ScsIntegrationTest
@EnabledIfEnvironmentVariable(named = "AI_DASHSCOPE_API_KEY", matches = ".+")
@DisplayName("模型环境集成测试（可选）")
class ModelIntegrationTest extends ScsPostgresRedisITBase {

    @Autowired
    private PromptTemplateProvider promptTemplateProvider;

    @Test
    @DisplayName("classpath FAQ Prompt 模板可读")
    void faqPromptTemplateReadable() {
        String content = promptTemplateProvider.get("faq-answer-system");
        assertThat(content).contains("知识片段");
    }
}
