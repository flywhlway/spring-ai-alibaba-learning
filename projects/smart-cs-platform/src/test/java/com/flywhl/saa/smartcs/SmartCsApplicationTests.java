package com.flywhl.saa.smartcs;

import org.elasticsearch.client.RestClient;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.context.ActiveProfiles;

import com.alibaba.cloud.ai.graph.agent.flow.agent.LlmRoutingAgent;
import com.flywhl.saa.smartcs.rag.FaqEtlPipeline;

import io.milvus.client.MilvusServiceClient;
import redis.clients.jedis.JedisPooled;

/**
 * 应用上下文冒烟测试：无 Docker 时通过 @MockBean 替代外部中间件与 Agent，仅验证 Spring 容器启动。
 *
 * @author flywhl
 */
@SpringBootTest(properties = {
        "spring.autoconfigure.exclude="
                + "org.springframework.ai.vectorstore.milvus.autoconfigure.MilvusVectorStoreAutoConfiguration,"
                + "org.springframework.ai.vectorstore.elasticsearch.autoconfigure.ElasticsearchVectorStoreAutoConfiguration,"
                + "org.springframework.ai.vectorstore.redis.autoconfigure.RedisVectorStoreAutoConfiguration,"
                + "org.springframework.boot.autoconfigure.elasticsearch.ElasticsearchRestClientAutoConfiguration"
})
@ActiveProfiles("test")
@DisplayName("应用上下文加载")
class SmartCsApplicationTests {

    @MockBean(name = "milvusVectorStore")
    private VectorStore milvusVectorStore;

    @MockBean(name = "elasticsearchVectorStore")
    private VectorStore elasticsearchVectorStore;

    @MockBean(name = "redisStackVectorStore")
    private VectorStore redisStackVectorStore;

    @MockBean(name = "csIntentRouter")
    private LlmRoutingAgent csIntentRouter;

    @MockBean
    private StringRedisTemplate stringRedisTemplate;

    @MockBean
    private RestClient restClient;

    @MockBean
    private MilvusServiceClient milvusServiceClient;

    @MockBean
    private JedisPooled scsCacheJedisPooled;

    @MockBean
    private FaqEtlPipeline faqEtlPipeline;

    @Test
    @DisplayName("Spring 上下文可正常启动")
    void contextLoads() {
    }
}
