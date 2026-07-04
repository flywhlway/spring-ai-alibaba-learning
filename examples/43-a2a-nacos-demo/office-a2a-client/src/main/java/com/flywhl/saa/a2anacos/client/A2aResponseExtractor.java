package com.flywhl.saa.a2anacos.client;

import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.cloud.ai.graph.agent.a2a.A2aRemoteAgent;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;

import java.util.List;
import java.util.Optional;

/**
 * 从 A2aRemoteAgent invoke 结果提取助手文本。
 *
 * @author flywhl
 */
final class A2aResponseExtractor {

    private A2aResponseExtractor() {
    }

    static String extractText(Optional<OverAllState> stateOptional) {
        if (stateOptional.isEmpty()) {
            return "";
        }
        OverAllState state = stateOptional.get();
        List<Message> messages = state.value("messages", List.of());
        for (int i = messages.size() - 1; i >= 0; i--) {
            Message message = messages.get(i);
            if (message instanceof AssistantMessage assistant) {
                String text = assistant.getText();
                if (text != null && !text.isBlank()) {
                    return text;
                }
            }
        }
        return state.value("output", "");
    }
}
