package com.flywhl.saa.rageval;

import com.flywhl.saa.rageval.model.EvalCase;
import com.flywhl.saa.rageval.model.EvalScore;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 最小 RAG 评测：规则法忠实度/相关性 + 内存响应缓存。
 *
 * <p>忠实度 = 答案命中证据关键词比例（答案是否 grounded 在知识库要点上）；
 * 相关性 = 答案命中问题相关关键词比例（答案是否回应问题）。
 * 二者均为启发式，非 LLM-as-judge，适合教学演示与回归冒烟。
 *
 * @author flywhl
 */
@Service
public class RagEvalService {

    private final ChatClient chatClient;
    private final VectorStore vectorStore;
    private final Map<String, String> answerCache = new ConcurrentHashMap<>();

    /** 内置固定问答集。 */
    static final List<EvalCase> DEFAULT_CASES = List.of(
            new EvalCase(
                    "ota-fail",
                    "OTA升级失败一般是什么原因？",
                    List.of("网络", "签名", "存储"),
                    List.of("OTA", "升级", "失败")),
            new EvalCase(
                    "bms-cold",
                    "低温下电池充电功率为什么会受限？",
                    List.of("BMS", "低温", "保护"),
                    List.of("电池", "充电", "低温")),
            new EvalCase(
                    "unrelated",
                    "今天上海天气怎么样？",
                    List.of(),
                    List.of("天气", "上海")));

    public RagEvalService(ChatClient chatClient, VectorStore vectorStore) {
        this.chatClient = chatClient;
        this.vectorStore = vectorStore;
    }

    public void ensureKnowledgeBase() {
        List<Document> documents = List.of(
                new Document(
                        "OTA 升级失败常见原因包括：网络中断导致包体传输不完整、签名校验失败、存储空间不足。",
                        Map.of("source", "OTA故障排查手册.pdf")),
                new Document(
                        "车辆诊断：电池管理系统（BMS）在低温环境下可能限制充电功率，属于正常保护策略。",
                        Map.of("source", "BMS诊断手册.pdf")));
        vectorStore.add(documents);
    }

    public List<EvalScore> run(List<EvalCase> cases) {
        return cases.stream().map(this::evaluate).toList();
    }

    private EvalScore evaluate(EvalCase evalCase) {
        boolean cached = answerCache.containsKey(evalCase.question());
        String answer = answerCache.computeIfAbsent(evalCase.question(), q ->
                chatClient.prompt().user(q).call().content());

        double faithfulness = keywordCoverage(answer, evalCase.evidenceKeywords());
        double relevance = keywordCoverage(answer, evalCase.relevanceKeywords());

        // 无关问题：若模型正确拒绝编造，给忠实度满分（未引入幻觉证据）
        if (evalCase.evidenceKeywords().isEmpty()) {
            faithfulness = answer.contains("未找到") || answer.contains("无法") || answer.contains("没有")
                    ? 1.0
                    : 0.0;
        }

        return new EvalScore(
                evalCase.id(),
                evalCase.question(),
                answer,
                faithfulness,
                relevance,
                cached);
    }

    static double keywordCoverage(String text, List<String> keywords) {
        if (keywords == null || keywords.isEmpty()) {
            return 0.0;
        }
        String lower = text == null ? "" : text.toLowerCase(Locale.ROOT);
        long hits = keywords.stream()
                .filter(k -> lower.contains(k.toLowerCase(Locale.ROOT)))
                .count();
        return (double) hits / keywords.size();
    }

    public int cacheSize() {
        return answerCache.size();
    }

    public void clearCache() {
        answerCache.clear();
    }
}
