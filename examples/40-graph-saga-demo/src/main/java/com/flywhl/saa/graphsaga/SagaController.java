package com.flywhl.saa.graphsaga;

import com.alibaba.cloud.ai.graph.CompiledGraph;
import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.cloud.ai.graph.RunnableConfig;
import com.alibaba.cloud.ai.graph.exception.GraphRunnerException;
import com.flywhl.saa.common.result.Result;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Saga 补偿图 REST 入口；forceFail=true 触发补偿路径。
 *
 * @author flywhl
 */
@RestController
public class SagaController {

    private final CompiledGraph sagaGraph;
    private final SagaNodes sagaNodes;

    public SagaController(CompiledGraph sagaGraph, SagaNodes sagaNodes) {
        this.sagaGraph = sagaGraph;
        this.sagaNodes = sagaNodes;
    }

    @GetMapping("/graph/saga")
    public Result<SagaOutcome> saga(
            @RequestParam String orderId,
            @RequestParam(defaultValue = "false") boolean forceFail) throws GraphRunnerException {
        RunnableConfig config = RunnableConfig.builder()
                .threadId(UUID.randomUUID().toString())
                .build();
        Map<String, Object> input = new HashMap<>();
        input.put("orderId", orderId);
        input.put("forceFail", forceFail);
        input.put("compensated", false);
        input.put("paymentSuccess", false);

        Optional<OverAllState> state = sagaGraph.invoke(input, config);
        OverAllState finalState = state.orElseThrow();
        boolean paymentSuccess = Boolean.TRUE.equals(finalState.value("paymentSuccess").orElse(false));
        boolean compensated = Boolean.TRUE.equals(finalState.value("compensated").orElse(false));
        String message = finalState.value("message", "");
        int inventory = finalState.value("inventory", sagaNodes.currentInventory());

        return Result.ok(new SagaOutcome(orderId, paymentSuccess, compensated, message, inventory));
    }
}
