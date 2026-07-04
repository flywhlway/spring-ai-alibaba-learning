package com.flywhl.saa.graphparallel;

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

import java.util.List;
import java.util.Map;

import static com.alibaba.cloud.ai.graph.action.AsyncNodeAction.node_async;

/**
 * 并行图：fan-out（START→List）+ fan-in（List→merge），无 addAggregatedEdge。
 *
 * @author flywhl
 */
@Configuration(proxyBeanMethods = false)
public class ParallelGraphConfig {

    @Bean
    public CompiledGraph parallelGraph(ParallelNodes nodes) throws GraphStateException {
        StateGraph graph = new StateGraph("parallel-diagnosis", (KeyStrategyFactory) () -> Map.of(
                "question", KeyStrategy.REPLACE,
                "kbResults", KeyStrategy.REPLACE,
                "historyResults", KeyStrategy.REPLACE,
                "answer", KeyStrategy.REPLACE));

        graph.addNode("searchKb", node_async(nodes::searchKnowledgeBase))
                .addNode("searchHistory", node_async(nodes::searchTicketHistory))
                .addNode("generateAnswer", node_async(nodes::generateAnswer))
                .addEdge(StateGraph.START, List.of("searchKb", "searchHistory"))
                .addEdge(List.of("searchKb", "searchHistory"), "generateAnswer")
                .addEdge("generateAnswer", StateGraph.END);

        return graph.compile(CompileConfig.builder()
                .saverConfig(SaverConfig.builder().register(new MemorySaver()).build())
                .build());
    }
}
