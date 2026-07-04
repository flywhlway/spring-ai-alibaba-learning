package com.flywhl.saa.agentdemo;

import com.alibaba.cloud.ai.graph.agent.ReactAgent;
import com.alibaba.cloud.ai.graph.exception.GraphRunnerException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 冒烟 IT：有 DashScope Key 时验证 ReactAgent.call 返回非空白文本。
 *
 * @author flywhl
 */
@SpringBootTest
@EnabledIfEnvironmentVariable(named = "AI_DASHSCOPE_API_KEY", matches = ".+")
class AgentDemoApplicationIT {

    @Autowired
    private ReactAgent vehicleDiagnosisAgent;

    @Test
    void diagnoseReturnsText() throws GraphRunnerException {
        assertThat(vehicleDiagnosisAgent.call("P0420是什么").getText()).isNotBlank();
    }
}
