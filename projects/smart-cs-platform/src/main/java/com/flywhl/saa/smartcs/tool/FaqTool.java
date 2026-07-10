package com.flywhl.saa.smartcs.tool;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import com.flywhl.saa.smartcs.model.vo.ChatAnswerVO;
import com.flywhl.saa.smartcs.service.FaqAnswerService;

/**
 * FAQ 问答 Tool：包装 {@link FaqAnswerService#answer(String)}（语义缓存 → Milvus/ES 混合检索
 * → RAG 生成 → 回写缓存全链路），供 {@code faq-agent} 挂载调用，避免 ReactAgent 绕过既有链路
 * 凭空生成答案。
 *
 * @author flywhl
 * @since 1.0.0
 */
@Component
public class FaqTool {

    private final FaqAnswerService faqAnswerService;

    public FaqTool(FaqAnswerService faqAnswerService) {
        this.faqAnswerService = faqAnswerService;
    }

    @Tool(description = "查询 FAQ 知识库并生成答案（语义缓存优先命中，未命中走 Milvus+ES 混合检索 + RAG 生成）")
    public String answerFaq(@ToolParam(description = "用户问题原文") String question) {
        ChatAnswerVO answer = faqAnswerService.answer(question);
        return answer.answer();
    }
}
