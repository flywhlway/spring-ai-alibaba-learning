package com.flywhl.saa.a2anacos.client;

import com.alibaba.cloud.ai.graph.RunnableConfig;
import com.alibaba.cloud.ai.graph.agent.a2a.A2aRemoteAgent;
import com.alibaba.cloud.ai.graph.exception.GraphRunnerException;
import com.flywhl.saa.common.result.Result;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/**
 * 通过 A2A 远程调用库存 Agent。
 *
 * @author flywhl
 */
@RestController
public class InventoryA2aController {

    private final A2aRemoteAgent inventoryRemoteAgent;

    public InventoryA2aController(A2aRemoteAgent inventoryRemoteAgent) {
        this.inventoryRemoteAgent = inventoryRemoteAgent;
    }

    @GetMapping("/a2a/inventory")
    public Result<String> inventory(@RequestParam String query) throws GraphRunnerException {
        RunnableConfig config = RunnableConfig.builder()
                .threadId(UUID.randomUUID().toString())
                .build();
        var state = inventoryRemoteAgent.invoke(query, config);
        return Result.ok(A2aResponseExtractor.extractText(state));
    }
}
