package com.flywhl.saa.workflow;

import com.alibaba.cloud.ai.graph.CompiledGraph;
import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.cloud.ai.graph.RunnableConfig;
import com.alibaba.cloud.ai.graph.exception.GraphRunnerException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 冒烟 IT：有 DashScope Key 时验证线性图 invoke 产出非空 answer。
 *
 * @author flywhl
 */
@SpringBootTest
@EnabledIfEnvironmentVariable(named = "AI_DASHSCOPE_API_KEY", matches = ".+")
class WorkflowDemoApplicationIT {

    @Autowired
    private CompiledGraph workflowGraph;

    @Test
    void runProducesNonEmptyAnswer() throws GraphRunnerException {
        RunnableConfig config = RunnableConfig.builder()
                .threadId(UUID.randomUUID().toString())
                .build();
        Optional<OverAllState> state = workflowGraph.invoke(
                Map.of("question", "P0420故障码是什么问题？"), config);

        assertThat(state).isPresent();
        assertThat(state.get().value("answer", "")).isNotBlank();
    }
}
