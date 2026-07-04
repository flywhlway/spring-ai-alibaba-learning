package com.flywhl.saa.rageval;

import com.flywhl.saa.common.result.Result;
import com.flywhl.saa.rageval.model.EvalCase;
import com.flywhl.saa.rageval.model.EvalScore;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * RAG 评测端点：运行内置或自定义问答集，返回忠实度/相关性分数。
 *
 * @author flywhl
 */
@RestController
@RequestMapping("/eval")
public class RagEvalController {

    private final RagEvalService ragEvalService;

    public RagEvalController(RagEvalService ragEvalService) {
        this.ragEvalService = ragEvalService;
    }

    /**
     * 运行评测。请求体可为空或省略，使用内置问答集；也可 POST 自定义 {@link EvalCase} 列表。
     */
    @PostMapping("/run")
    public Result<Map<String, Object>> run(@RequestBody(required = false) List<EvalCase> cases) {
        ragEvalService.ensureKnowledgeBase();
        List<EvalCase> suite = (cases == null || cases.isEmpty())
                ? RagEvalService.DEFAULT_CASES
                : cases;
        List<EvalScore> scores = ragEvalService.run(suite);

        double avgFaithfulness = scores.stream().mapToDouble(EvalScore::faithfulness).average().orElse(0);
        double avgRelevance = scores.stream().mapToDouble(EvalScore::relevance).average().orElse(0);

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("scores", scores);
        payload.put("avgFaithfulness", avgFaithfulness);
        payload.put("avgRelevance", avgRelevance);
        payload.put("cacheSize", ragEvalService.cacheSize());
        return Result.ok(payload);
    }

    /** 清空响应缓存（演示降本：重复问题命中缓存）。 */
    @PostMapping("/cache/clear")
    public Result<Map<String, Object>> clearCache() {
        ragEvalService.clearCache();
        return Result.ok(Map.of("cacheSize", 0));
    }
}
