package com.flywhl.saa.workflow;

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

import static com.alibaba.cloud.ai.graph.action.AsyncNodeAction.node_async;

/**
 * 线性图：START → rewrite → retrieve → generate → END，进程内 MemorySaver。
 *
 * @author flywhl
 */
@Configuration(proxyBeanMethods = false)
public class WorkflowGraphConfig {

    @Bean
    public CompiledGraph workflowGraph(WorkflowNodes nodes) throws GraphStateException {
        StateGraph graph = new StateGraph("workflow-demo", (KeyStrategyFactory) () -> Map.of(
                "question", KeyStrategy.REPLACE,
                "rewrittenQuery", KeyStrategy.REPLACE,
                "evidence", KeyStrategy.REPLACE,
                "answer", KeyStrategy.REPLACE));

        graph.addNode("rewrite", node_async(nodes::rewrite))
                .addNode("retrieve", node_async(nodes::retrieve))
                .addNode("generate", node_async(nodes::generate))
                .addEdge(StateGraph.START, "rewrite")
                .addEdge("rewrite", "retrieve")
                .addEdge("retrieve", "generate")
                .addEdge("generate", StateGraph.END);

        return graph.compile(CompileConfig.builder()
                .saverConfig(SaverConfig.builder().register(new MemorySaver()).build())
                .build());
    }
}
