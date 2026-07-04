package com.flywhl.saa.multiagent;

import com.alibaba.cloud.ai.graph.RunnableConfig;
import com.alibaba.cloud.ai.graph.agent.flow.agent.SequentialAgent;
import com.alibaba.cloud.ai.graph.exception.GraphRunnerException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 冒烟 IT：有 DashScope Key 时验证 SequentialAgent invoke 产出非空文本。
 *
 * @author flywhl
 */
@SpringBootTest
@EnabledIfEnvironmentVariable(named = "AI_DASHSCOPE_API_KEY", matches = ".+")
class MultiAgentDemoApplicationIT {

    @Autowired
    private SequentialAgent sequentialPipeline;

    @Test
    void sequentialProducesNonEmptyText() throws GraphRunnerException {
        RunnableConfig config = RunnableConfig.builder()
                .threadId(UUID.randomUUID().toString())
                .build();
        var state = sequentialPipeline.invoke("P0420故障码怎么处理？", config);

        assertThat(state).isPresent();
        assertThat(FlowStateExtractor.extractText(state)).isNotBlank();
    }
}
