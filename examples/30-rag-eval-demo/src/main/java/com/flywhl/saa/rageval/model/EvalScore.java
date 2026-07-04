package com.flywhl.saa.rageval.model;

/**
 * 单条评测结果。
 *
 * @param caseId       用例 ID
 * @param question     问题
 * @param answer       模型答案
 * @param faithfulness 忠实度 [0,1]：答案覆盖证据关键词的比例
 * @param relevance    相关性 [0,1]：答案覆盖问题相关关键词的比例
 * @param cached       是否命中响应缓存
 * @author flywhl
 */
public record EvalScore(
        String caseId,
        String question,
        String answer,
        double faithfulness,
        double relevance,
        boolean cached) {
}
