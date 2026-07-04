package com.flywhl.saa.graphsaga;

import com.alibaba.cloud.ai.graph.CompileConfig;
import com.alibaba.cloud.ai.graph.CompiledGraph;
import com.alibaba.cloud.ai.graph.KeyStrategy;
import com.alibaba.cloud.ai.graph.KeyStrategyFactory;
import com.alibaba.cloud.ai.graph.StateGraph;
import com.alibaba.cloud.ai.graph.checkpoint.config.SaverConfig;
import com.alibaba.cloud.ai.graph.checkpoint.savers.MemorySaver;
import com.alibaba.cloud.ai.graph.exception.GraphStateException;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

import static com.alibaba.cloud.ai.graph.action.AsyncEdgeAction.edge_async;
import static com.alibaba.cloud.ai.graph.action.AsyncNodeAction.node_async;

/**
 * 最小 Saga：扣库存 → 扣款 → 条件边（成功 END / 失败 compensateInventory）。
 *
 * @author flywhl
 */
@Configuration(proxyBeanMethods = false)
public class SagaGraphConfig {

    @Bean
    public CompiledGraph sagaGraph(SagaNodes nodes) throws GraphStateException {
        StateGraph graph = new StateGraph("graph-saga", (KeyStrategyFactory) () -> Map.of(
                "orderId", KeyStrategy.REPLACE,
                "forceFail", KeyStrategy.REPLACE,
                "paymentSuccess", KeyStrategy.REPLACE,
                "compensated", KeyStrategy.REPLACE,
                "inventory", KeyStrategy.REPLACE,
                "message", KeyStrategy.REPLACE));

        graph.addNode("deductInventory", node_async(nodes::deductInventory))
                .addNode("chargePayment", node_async(nodes::chargePayment))
                .addNode("compensateInventory", node_async(nodes::compensateInventory))
                .addEdge(StateGraph.START, "deductInventory")
                .addEdge("deductInventory", "chargePayment")
                .addConditionalEdges("chargePayment",
                        edge_async(state -> Boolean.TRUE.equals(state.value("paymentSuccess").orElse(false))
                                ? "success"
                                : "failure"),
                        Map.of(
                                "success", StateGraph.END,
                                "failure", "compensateInventory"))
                .addEdge("compensateInventory", StateGraph.END);

        return graph.compile(CompileConfig.builder()
                .saverConfig(SaverConfig.builder().register(new MemorySaver()).build())
                .build());
    }
}
