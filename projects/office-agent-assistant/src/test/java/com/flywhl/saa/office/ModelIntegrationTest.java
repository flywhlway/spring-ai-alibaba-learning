package com.flywhl.saa.office;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.redis.core.StringRedisTemplate;

import com.alibaba.cloud.ai.graph.agent.ReactAgent;
import com.alibaba.cloud.ai.graph.agent.flow.agent.LlmRoutingAgent;
import com.alibaba.cloud.ai.graph.agent.flow.agent.SequentialAgent;
import com.flywhl.saa.office.prompt.PromptTemplateProvider;
import com.flywhl.saa.office.support.OfficeIntegrationTest;
import com.flywhl.saa.office.support.OfficeMySqlITBase;

/**
 * 模型环境集成测试（需 AI_DASHSCOPE_API_KEY + Docker）。
 *
 * @author flywhl
 */
@OfficeIntegrationTest
@EnabledIfEnvironmentVariable(named = "AI_DASHSCOPE_API_KEY", matches = ".+")
@DisplayName("模型环境集成测试")
class ModelIntegrationTest extends OfficeMySqlITBase {

    @Autowired
    private PromptTemplateProvider promptTemplateProvider;

    @MockBean
    private VectorStore vectorStore;

    @MockBean
    private StringRedisTemplate stringRedisTemplate;

    @MockBean(name = "officeAssistantAgent")
    private ReactAgent officeAssistantAgent;

    @MockBean(name = "approvalSequentialPipeline")
    private SequentialAgent approvalSequentialPipeline;

    @MockBean(name = "approvalRoutingAgent")
    private LlmRoutingAgent approvalRoutingAgent;

    @Test
    @DisplayName("Prompt 模板可读")
    void promptTemplateReadable() {
        String content = promptTemplateProvider.get("meeting-summary");
        org.assertj.core.api.Assertions.assertThat(content).contains("会议");
    }
}
