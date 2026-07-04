package com.flywhl.saa.hybridrag;

import com.flywhl.saa.common.result.Result;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.document.Document;
import org.springframework.ai.rag.Query;
import org.springframework.ai.rag.advisor.RetrievalAugmentationAdvisor;
import org.springframework.ai.rag.retrieval.search.VectorStoreDocumentRetriever;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Hybrid RAG + Citation：检索证据后应用层拼接 citations（非模型自述）。
 *
 * <p>对齐教程第 09 章可运行 Demo；响应经 {@link Result} 统一包装。
 *
 * @author flywhl
 */
@RestController
public class RagController {

    private final ChatClient chatClient;
    private final VectorStoreDocumentRetriever retriever;
    private final VectorStore vectorStore;

    public RagController(ChatClient.Builder chatClientBuilder, VectorStore vectorStore) {
        this.vectorStore = vectorStore;
        this.retriever = VectorStoreDocumentRetriever.builder()
                .vectorStore(vectorStore)
                .similarityThreshold(0.6)
                .topK(5)
                .build();

        var ragAdvisor = RetrievalAugmentationAdvisor.builder()
                .documentRetriever(retriever)
                .build();

        this.chatClient = chatClientBuilder
                .defaultSystem("""
                        你是企业知识库助手。你只能依据检索到的知识库内容回答用户问题。
                        如果知识库中没有相关资料，请明确回复「知识库中未找到相关信息」，不要编造事实。
                        """)
                .defaultAdvisors(ragAdvisor)
                .build();
    }

    /**
     * 写入教程示例对应的样例文档，便于本地 curl 验证 Citation。
     */
    @PostMapping("/ingest")
    public Result<Map<String, Object>> ingest() {
        List<Document> documents = List.of(
                new Document(
                        "OTA 升级失败常见原因包括：网络中断导致包体传输不完整、签名校验失败、存储空间不足。",
                        Map.of("source", "OTA故障排查手册.pdf", "page", "12")),
                new Document(
                        "车联网平台运维规范要求：OTA 任务失败后应检查设备在线状态、差分包完整性与签名证书有效期。",
                        Map.of("source", "车联网平台运维规范.docx", "page", "8")),
                new Document(
                        "差分包下载中断后应支持断点续传；签名校验失败时禁止刷写，需重新拉取官方包。",
                        Map.of("source", "OTA故障排查手册.pdf", "page", "15")),
                new Document(
                        "运维值班手册：OTA 失败率超过阈值时需升级 P1 故障并通知车云平台值班。",
                        Map.of("source", "车联网平台运维规范.docx", "page", "22")));
        vectorStore.add(documents);
        return Result.ok(Map.of("ingested", documents.size()));
    }

    @GetMapping("/ask")
    public Result<Map<String, Object>> ask(@RequestParam String question) {
        List<Document> evidences = retriever.retrieve(new Query(question));
        String answer = chatClient.prompt().user(question).call().content();

        List<String> citations = evidences.stream()
                .map(doc -> String.valueOf(doc.getMetadata().getOrDefault("source", "未知来源")))
                .distinct()
                .toList();

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("answer", answer);
        payload.put("citations", citations);
        payload.put("evidenceCount", evidences.size());
        return Result.ok(payload);
    }
}
