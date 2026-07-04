package com.flywhl.saa.embedding;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 冒烟 IT：有 DashScope Key 时断言向量非空。
 *
 * @author flywhl
 */
@SpringBootTest
@EnabledIfEnvironmentVariable(named = "AI_DASHSCOPE_API_KEY", matches = ".+")
class EmbeddingDemoApplicationIT {

    @Autowired
    private EmbeddingModel embeddingModel;

    @Test
    void embedProducesNonEmptyVector() {
        float[] vector = embeddingModel.embed("Spring AI Alibaba 企业级实战教程");
        assertThat(vector).isNotEmpty();
        assertThat(vector.length).isEqualTo(1024);
    }
}
