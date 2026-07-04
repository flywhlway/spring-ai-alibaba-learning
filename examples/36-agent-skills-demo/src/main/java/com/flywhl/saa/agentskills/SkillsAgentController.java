package com.flywhl.saa.agentskills;

import com.alibaba.cloud.ai.graph.agent.ReactAgent;
import com.alibaba.cloud.ai.graph.exception.GraphRunnerException;
import com.flywhl.saa.common.result.Result;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Skills Agent REST 入口。
 *
 * @author flywhl
 */
@RestController
public class SkillsAgentController {

    private final ReactAgent multiSkillAgent;

    public SkillsAgentController(ReactAgent multiSkillAgent) {
        this.multiSkillAgent = multiSkillAgent;
    }

    @GetMapping("/agent/skills")
    public Result<String> skills(@RequestParam String query) throws GraphRunnerException {
        AssistantMessage result = multiSkillAgent.call(query);
        return Result.ok(result.getText());
    }
}
