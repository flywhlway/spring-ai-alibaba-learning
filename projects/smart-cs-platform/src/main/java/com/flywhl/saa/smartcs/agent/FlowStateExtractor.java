package com.flywhl.saa.smartcs.agent;

import java.util.List;
import java.util.Optional;

import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;

import com.alibaba.cloud.ai.graph.OverAllState;

/**
 * 从 FlowAgent/ReactAgent {@code invoke} 返回的 {@link OverAllState} 提取最终助手文本
 * （复制 {@code examples/41-multi-agent-demo} 真源写法，跨包公开供
 * {@code service.CsOrchestratorService} 调用）。
 *
 * @author flywhl
 * @since 1.0.0
 */
public final class FlowStateExtractor {

    private FlowStateExtractor() {
    }

    public static String extractText(Optional<OverAllState> stateOptional) {
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
