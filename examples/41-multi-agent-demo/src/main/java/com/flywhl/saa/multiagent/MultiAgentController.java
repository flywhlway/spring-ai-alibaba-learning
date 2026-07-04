package com.flywhl.saa.multiagent;

import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.cloud.ai.graph.RunnableConfig;
import com.alibaba.cloud.ai.graph.agent.flow.agent.LlmRoutingAgent;
import com.alibaba.cloud.ai.graph.agent.flow.agent.LoopAgent;
import com.alibaba.cloud.ai.graph.agent.flow.agent.ParallelAgent;
import com.alibaba.cloud.ai.graph.agent.flow.agent.SequentialAgent;
import com.alibaba.cloud.ai.graph.exception.GraphRunnerException;
import com.flywhl.saa.common.result.Result;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;
import java.util.UUID;

/**
 * 四模式 FlowAgent REST 入口。
 *
 * @author flywhl
 */
@RestController
public class MultiAgentController {

    private final SequentialAgent sequentialPipeline;
    private final ParallelAgent parallelSearch;
    private final LlmRoutingAgent customerServiceRouter;
    private final LoopAgent refineLoop;

    public MultiAgentController(SequentialAgent sequentialPipeline,
                                ParallelAgent parallelSearch,
                                LlmRoutingAgent customerServiceRouter,
                                LoopAgent refineLoop) {
        this.sequentialPipeline = sequentialPipeline;
        this.parallelSearch = parallelSearch;
        this.customerServiceRouter = customerServiceRouter;
        this.refineLoop = refineLoop;
    }

    @GetMapping("/multi/sequential")
    public Result<String> sequential(@RequestParam String query) throws GraphRunnerException {
        return Result.ok(invokeFlow(sequentialPipeline, query));
    }

    @GetMapping("/multi/parallel")
    public Result<String> parallel(@RequestParam String query) throws GraphRunnerException {
        return Result.ok(invokeFlow(parallelSearch, query));
    }

    @GetMapping("/multi/routing")
    public Result<String> routing(@RequestParam String query) throws GraphRunnerException {
        return Result.ok(invokeFlow(customerServiceRouter, query));
    }

    @GetMapping("/multi/loop")
    public Result<String> loop(@RequestParam String query) throws GraphRunnerException {
        return Result.ok(invokeFlow(refineLoop, query));
    }

    private String invokeFlow(com.alibaba.cloud.ai.graph.agent.Agent flowAgent, String query)
            throws GraphRunnerException {
        RunnableConfig config = RunnableConfig.builder()
                .threadId(UUID.randomUUID().toString())
                .build();
        Optional<OverAllState> state = flowAgent.invoke(query, config);
        return FlowStateExtractor.extractText(state);
    }
}
