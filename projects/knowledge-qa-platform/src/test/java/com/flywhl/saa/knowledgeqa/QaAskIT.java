package com.flywhl.saa.knowledgeqa;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import com.flywhl.saa.knowledgeqa.config.DemoKnowledgeSeeder;
import com.flywhl.saa.knowledgeqa.support.KqaPostgresRedisITBase;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.minio.MinioClient;

/**
 * 同步问答 IT：需 AI_DASHSCOPE_API_KEY 与 Milvus 向量索引（手动 infra profile）。
 *
 * <p>无 Key 时自动 Disabled，不阻塞 CI。
 *
 * @author flywhl
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("it")
@EnabledIfEnvironmentVariable(named = "AI_DASHSCOPE_API_KEY", matches = ".+")
@DisplayName("同步问答 IT")
class QaAskIT extends KqaPostgresRedisITBase {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private DemoKnowledgeSeeder demoKnowledgeSeeder;

    @MockBean
    private MinioClient minioClient;

    private String userToken;

    @BeforeEach
    void login() throws Exception {
        String body = "{\"username\":\"zhangsan\",\"password\":\"zhangsan123\"}";
        ResponseEntity<String> loginResp = restTemplate.postForEntity(
                "http://localhost:" + port + "/api/auth/login",
                new HttpEntity<>(body, jsonHeaders()),
                String.class);
        assertThat(loginResp.getStatusCode()).isEqualTo(HttpStatus.OK);
        JsonNode root = objectMapper.readTree(loginResp.getBody());
        userToken = root.path("data").path("accessToken").asText();
        assertThat(userToken).isNotBlank();
    }

    @Test
    @DisplayName("zhangsan 提问返回 citations 非空")
    void askReturnsCitations() throws Exception {
        String askBody = """
                {"conversationId":"it-conv-ask","question":"员工出差住宿费报销标准是多少？"}
                """;
        HttpHeaders headers = jsonHeaders();
        headers.setBearerAuth(userToken);

        ResponseEntity<String> response = restTemplate.exchange(
                "http://localhost:" + port + "/api/qa/ask",
                HttpMethod.POST,
                new HttpEntity<>(askBody, headers),
                String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        JsonNode root = objectMapper.readTree(response.getBody());
        assertThat(root.path("code").asInt()).isZero();
        assertThat(root.path("data").path("citations").isArray()).isTrue();
        assertThat(root.path("data").path("citations").size()).isPositive();
    }

    private static HttpHeaders jsonHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }
}
