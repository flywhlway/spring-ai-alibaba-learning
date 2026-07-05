package com.flywhl.saa.knowledgeqa;

/**
 * 集成测试骨架占位。
 *
 * <p>后续迭代按仓库测试约定补齐：
 * <ul>
 *   <li>PostgreSQL/Redis 用 Testcontainers（{@code @Testcontainers} + {@code @DynamicPropertySource}）；</li>
 *   <li>真实模型调用用例标注 {@code @EnabledIfEnvironmentVariable(named = "AI_DASHSCOPE_API_KEY", matches = ".+")}，
 *       保证无 Key 环境 {@code mvn clean install} 全绿。</li>
 * </ul>
 *
 * @author flywhl
 * @since 1.0.0
 */
class KnowledgeQaApplicationTests {
}
