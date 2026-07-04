package com.flywhl.saa.advancedrag;

import com.flywhl.saa.common.result.Result;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * Advanced RAG：入库 + 查询改写/重排序问答。
 *
 * @author flywhl
 */
@RestController
public class AdvancedRagController {

    private final ChatClient chatClient;
    private final VectorStore vectorStore;

    public AdvancedRagController(ChatClient chatClient, VectorStore vectorStore) {
        this.chatClient = chatClient;
        this.vectorStore = vectorStore;
    }

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
                        "车辆诊断：电池管理系统（BMS）在低温环境下可能限制充电功率，属于正常保护策略。",
                        Map.of("source", "BMS诊断手册.pdf", "page", "3")));
        vectorStore.add(documents);
        return Result.ok(Map.of("ingested", documents.size()));
    }

    @GetMapping("/ask")
    public Result<String> ask(@RequestParam String question) {
        String answer = chatClient.prompt().user(question).call().content();
        return Result.ok(answer);
    }
}
