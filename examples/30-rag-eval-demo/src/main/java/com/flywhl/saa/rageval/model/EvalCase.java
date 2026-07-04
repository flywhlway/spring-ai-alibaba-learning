package com.flywhl.saa.rageval.model;

import java.util.List;

/**
 * 单条评测用例。
 *
 * @param id               用例 ID
 * @param question         用户问题
 * @param evidenceKeywords 证据关键词（用于忠实度：答案是否覆盖证据要点）
 * @param relevanceKeywords 问题相关关键词（用于相关性：答案是否回应问题）
 * @author flywhl
 */
public record EvalCase(
        String id,
        String question,
        List<String> evidenceKeywords,
        List<String> relevanceKeywords) {
}
