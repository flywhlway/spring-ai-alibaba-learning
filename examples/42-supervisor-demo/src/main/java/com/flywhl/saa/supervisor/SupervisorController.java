package com.flywhl.saa.supervisor;

import com.alibaba.cloud.ai.graph.agent.ReactAgent;
import com.alibaba.cloud.ai.graph.exception.GraphRunnerException;
import com.flywhl.saa.common.result.Result;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Supervisor REST 入口。
 *
 * @author flywhl
 */
@RestController
public class SupervisorController {

    private final ReactAgent officeSupervisor;

    public SupervisorController(ReactAgent officeSupervisor) {
        this.officeSupervisor = officeSupervisor;
    }

    @GetMapping("/supervisor/chat")
    public Result<String> chat(@RequestParam String query) throws GraphRunnerException {
        return Result.ok(officeSupervisor.call(query).getText());
    }
}
