package com.flywhl.saa.summarymemory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.CallAdvisor;
import org.springframework.ai.chat.client.advisor.api.CallAdvisorChain;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.core.Ordered;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 在 {@link org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor} 读取历史之前，
 * 若消息数达到阈值，则用模型把旧消息压缩为一段摘要，替换进 {@link ChatMemory}。
 *
 * <p>摘要调用走裸 {@link ChatModel}，避免再次进入 Advisor 链造成递归。
 *
 * @author flywhl
 */
public class SummaryCompressingAdvisor implements CallAdvisor {

    private static final Logger log = LoggerFactory.getLogger(SummaryCompressingAdvisor.class);

    private final ChatMemory chatMemory;
    private final ChatModel chatModel;
    private final int threshold;
    private final int keepRecent;

    public SummaryCompressingAdvisor(ChatMemory chatMemory, ChatModel chatModel,
                                     int threshold, int keepRecent) {
        this.chatMemory = chatMemory;
        this.chatModel = chatModel;
        this.threshold = threshold;
        this.keepRecent = keepRecent;
    }

    @Override
    public String getName() {
        return "SummaryCompressingAdvisor";
    }

    @Override
    public int getOrder() {
        // 必须早于 MessageChatMemoryAdvisor，先压缩再让 Memory Advisor 读取
        return Ordered.HIGHEST_PRECEDENCE + 200;
    }

    @Override
    public ChatClientResponse adviseCall(ChatClientRequest request, CallAdvisorChain chain) {
        Object conversationIdObj = request.context().get(ChatMemory.CONVERSATION_ID);
        if (conversationIdObj instanceof String conversationId) {
            List<Message> history = chatMemory.get(conversationId);
            if (history.size() >= threshold) {
                compress(conversationId, history);
            }
        }
        return chain.nextCall(request);
    }

    private void compress(String conversationId, List<Message> history) {
        int split = Math.max(0, history.size() - keepRecent);
        List<Message> toSummarize = history.subList(0, split);
        List<Message> recent = new ArrayList<>(history.subList(split, history.size()));

        String transcript = toSummarize.stream()
                .map(m -> m.getMessageType().name() + ": " + m.getText())
                .collect(Collectors.joining("\n"));
        String summaryText = chatModel.call(
                "请将以下对话历史压缩为一段简洁中文摘要，保留关键事实与用户偏好，不要添加新信息：\n"
                        + transcript);

        List<Message> compressed = new ArrayList<>();
        compressed.add(new SystemMessage("【历史摘要】" + summaryText));
        compressed.addAll(recent);

        chatMemory.clear(conversationId);
        chatMemory.add(conversationId, compressed);
        log.info("conversationId={} 已压缩：{} 条 → {} 条（含摘要）",
                conversationId, history.size(), compressed.size());
    }
}
