package com.flywhl.saa.workflow;

import com.alibaba.cloud.ai.graph.OverAllState;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 线性工作流节点：查询改写 → 内存检索 → 模型生成。
 *
 * @author flywhl
 */
@Component
public class WorkflowNodes {

    private final ChatClient chatClient;

    public WorkflowNodes(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder.build();
    }

    public Map<String, Object> rewrite(OverAllState state) {
        String question = state.value("question", "");
        String rewritten = question.strip().isEmpty()
                ? question
                : "车辆故障诊断：" + question.strip();
        return Map.of("rewrittenQuery", rewritten);
    }

    public Map<String, Object> retrieve(OverAllState state) {
        String query = state.value("rewrittenQuery", "");
        // 内存假数据，真实场景对接向量检索（第 09/11 章）
        String evidence = "知识库命中：P0420 常见于三元催化器老化；氧传感器漂移也可能触发同码。查询原文=" + query;
        return Map.of("evidence", evidence);
    }

    public Map<String, Object> generate(OverAllState state) {
        String evidence = state.value("evidence", "");
        String question = state.value("question", "");
        String answer = chatClient.prompt()
                .user("根据以下检索证据回答用户问题。\n问题：%s\n证据：%s".formatted(question, evidence))
                .call()
                .content();
        return Map.of("answer", answer == null ? "" : answer);
    }
}
