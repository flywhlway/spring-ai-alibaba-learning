package com.flywhl.saa.office;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.redis.core.StringRedisTemplate;

import com.alibaba.cloud.ai.graph.agent.ReactAgent;
import com.alibaba.cloud.ai.graph.agent.flow.agent.LlmRoutingAgent;
import com.alibaba.cloud.ai.graph.agent.flow.agent.SequentialAgent;
import com.flywhl.saa.office.support.OfficeIntegrationTest;
import com.flywhl.saa.office.support.OfficeMySqlITBase;

/**
 * 应用上下文冒烟测试（Testcontainers MySQL + Redis）。
 *
 * @author flywhl
 */
@OfficeIntegrationTest
@DisplayName("应用上下文")
class OfficeAgentApplicationTests extends OfficeMySqlITBase {

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
    @DisplayName("上下文可启动")
    void contextLoads() {
    }
}
