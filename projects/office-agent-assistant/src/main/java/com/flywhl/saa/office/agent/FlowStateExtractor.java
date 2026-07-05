package com.flywhl.saa.office.agent;

import com.alibaba.cloud.ai.graph.OverAllState;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import java.util.List;
import java.util.Optional;

public final class FlowStateExtractor {
    private FlowStateExtractor() {}
    public static String extractText(Optional<OverAllState> stateOptional) {
        if (stateOptional.isEmpty()) return "";
        OverAllState state = stateOptional.get();
        List<Message> messages = state.value("messages", List.of());
        for (int i = messages.size() - 1; i >= 0; i--) {
            Message message = messages.get(i);
            if (message instanceof AssistantMessage assistant) {
                String text = assistant.getText();
                if (text != null && !text.isBlank()) return text;
            }
        }
        return state.value("output", "");
    }
}

