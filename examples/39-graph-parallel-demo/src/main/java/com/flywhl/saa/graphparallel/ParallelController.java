package com.flywhl.saa.graphparallel;

import com.alibaba.cloud.ai.graph.CompiledGraph;
import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.cloud.ai.graph.RunnableConfig;
import com.alibaba.cloud.ai.graph.exception.GraphRunnerException;
import com.flywhl.saa.common.result.Result;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * 并行诊断图 REST 入口。
 *
 * @author flywhl
 */
@RestController
public class ParallelController {

    private final CompiledGraph parallelGraph;

    public ParallelController(CompiledGraph parallelGraph) {
        this.parallelGraph = parallelGraph;
    }

    @GetMapping("/graph/parallel")
    public Result<String> parallel(@RequestParam String question) throws GraphRunnerException {
        RunnableConfig config = RunnableConfig.builder()
                .threadId(UUID.randomUUID().toString())
                .build();
        Optional<OverAllState> state = parallelGraph.invoke(Map.of("question", question), config);
        String answer = state.flatMap(s -> s.<String>value("answer")).orElse("");
        return Result.ok(answer);
    }
}
