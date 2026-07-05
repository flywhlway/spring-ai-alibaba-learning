package com.flywhl.saa.office.service;

import com.alibaba.cloud.ai.graph.RunnableConfig;
import com.alibaba.cloud.ai.graph.agent.ReactAgent;
import com.alibaba.cloud.ai.graph.exception.GraphRunnerException;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import com.flywhl.saa.office.model.dto.ChatRequest;
import com.flywhl.saa.office.model.vo.ChatResponseVO;
import java.util.UUID;

@Service
public class ChatService {
    private final ReactAgent officeAssistantAgent;
    private final AuthService authService;
    public ChatService(@Qualifier("officeAssistantAgent") ReactAgent officeAssistantAgent, AuthService authService) {
        this.officeAssistantAgent = officeAssistantAgent;
        this.authService = authService;
    }
    public ChatResponseVO chat(ChatRequest request) throws GraphRunnerException {
        authService.requireCurrentUser();
        String conversationId = request.conversationId() != null && !request.conversationId().isBlank()
                ? request.conversationId() : UUID.randomUUID().toString();
        RunnableConfig config = RunnableConfig.builder().threadId(conversationId).build();
        AssistantMessage result = officeAssistantAgent.call(request.message(), config);
        return new ChatResponseVO(conversationId, result.getText());
    }
}

