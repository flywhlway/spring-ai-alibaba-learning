package com.flywhl.saa.agentdemo;

import com.alibaba.cloud.ai.graph.agent.ReactAgent;
import com.alibaba.cloud.ai.graph.exception.GraphRunnerException;
import com.flywhl.saa.common.result.Result;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 车辆诊断 Agent REST 入口。
 *
 * @author flywhl
 */
@RestController
public class AgentController {

    private final ReactAgent vehicleDiagnosisAgent;

    public AgentController(ReactAgent vehicleDiagnosisAgent) {
        this.vehicleDiagnosisAgent = vehicleDiagnosisAgent;
    }

    @GetMapping("/agent/diagnose")
    public Result<String> diagnose(@RequestParam String query) throws GraphRunnerException {
        AssistantMessage result = vehicleDiagnosisAgent.call(query);
        return Result.ok(result.getText());
    }
}
