package com.flywhl.saa.graphparallel;

import com.alibaba.cloud.ai.graph.OverAllState;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 并行检索节点（内存假数据）与汇总生成节点。
 *
 * @author flywhl
 */
@Component
public class ParallelNodes {

    private final ChatClient chatClient;

    public ParallelNodes(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder.build();
    }

    public Map<String, Object> searchKnowledgeBase(OverAllState state) {
        String question = state.value("question", "");
        return Map.of("kbResults",
                "知识库匹配：P0420一般与三元催化器老化相关。问题=" + question);
    }

    public Map<String, Object> searchTicketHistory(OverAllState state) {
        return Map.of("historyResults",
                "历史工单：3个月内同车型同故障码5起，4起为催化器更换解决");
    }

    public Map<String, Object> generateAnswer(OverAllState state) {
        String kb = state.value("kbResults", "");
        String history = state.value("historyResults", "");
        String answer = chatClient.prompt()
                .user("结合以下信息给出诊断建议：\n知识库：%s\n历史工单：%s".formatted(kb, history))
                .call()
                .content();
        return Map.of("answer", answer == null ? "" : answer);
    }
}
