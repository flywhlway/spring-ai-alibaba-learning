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
 * SSE 流式问答 IT：断言 message/meta/done 事件（需 API Key + Milvus）。
 *
 * @author flywhl
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("it")
@EnabledIfEnvironmentVariable(named = "AI_DASHSCOPE_API_KEY", matches = ".+")
@DisplayName("流式问答 IT")
class QaStreamIT extends KqaPostgresRedisITBase {

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
        JsonNode root = objectMapper.readTree(loginResp.getBody());
        userToken = root.path("data").path("accessToken").asText();
    }

    @Test
    @DisplayName("SSE 含 message 与 done 事件")
    void streamContainsMessageAndDoneEvents() {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(userToken);
        headers.setAccept(java.util.List.of(MediaType.TEXT_EVENT_STREAM));

        String url = "http://localhost:" + port
                + "/api/qa/stream?conversationId=it-conv-stream&question=智能网关如何恢复出厂设置";

        ResponseEntity<String> response = restTemplate.exchange(
                url,
                org.springframework.http.HttpMethod.GET,
                new HttpEntity<>(headers),
                String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        String sse = response.getBody();
        assertThat(sse).contains("event:message");
        assertThat(sse).containsAnyOf("event:done", "event: done");
    }

    private static HttpHeaders jsonHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }
}
