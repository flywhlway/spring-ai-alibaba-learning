package com.flywhl.saa.embedding;

import com.alibaba.cloud.ai.dashscope.embedding.DashScopeEmbeddingOptions;
import com.flywhl.saa.common.result.Result;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.embedding.EmbeddingRequest;
import org.springframework.ai.embedding.EmbeddingResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 不同维度下的向量化耗时、存储字节与 token 用量基准测试。
 *
 * @author flywhl
 */
@RestController
public class EmbeddingBenchmarkController {

    private static final int[] DIMENSIONS_TO_TEST = {64, 256, 1024, 2048};

    private final EmbeddingModel embeddingModel;

    public EmbeddingBenchmarkController(EmbeddingModel embeddingModel) {
        this.embeddingModel = embeddingModel;
    }

    @GetMapping("/embedding/benchmark")
    public Result<Map<String, Object>> benchmark(@RequestParam String text) {
        List<Map<String, Object>> results = new ArrayList<>();
        for (int dim : DIMENSIONS_TO_TEST) {
            long start = System.currentTimeMillis();
            EmbeddingResponse response = embeddingModel.call(new EmbeddingRequest(
                    List.of(text),
                    DashScopeEmbeddingOptions.builder()
                            .model("text-embedding-v4")
                            .dimensions(dim)
                            .build()));
            long costMs = System.currentTimeMillis() - start;
            float[] vector = response.getResults().getFirst().getOutput();
            long storageBytes = (long) vector.length * Float.BYTES;

            Map<String, Object> row = new LinkedHashMap<>();
            row.put("dimensions", dim);
            row.put("vectorLength", vector.length);
            row.put("costMs", costMs);
            row.put("storageBytesPerVector", storageBytes);
            row.put("estimatedTokenUsage", response.getMetadata().getUsage() != null
                    ? response.getMetadata().getUsage().getTotalTokens()
                    : -1);
            results.add(row);
        }
        return Result.ok(Map.of("results", results));
    }
}
