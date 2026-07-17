package com.flywhl.saa.smartcs;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import com.alibaba.cloud.ai.graph.RunnableConfig;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.flywhl.saa.smartcs.support.ScsIntegrationTest;
import com.flywhl.saa.smartcs.support.ScsPostgresRedisITBase;

/**
 * 会话问答集成测试：@MockBean csIntentRouter，无 API Key 断言 POST /api/chat/ask 200。
 *
 * @author flywhl
 */
@ScsIntegrationTest
@AutoConfigureMockMvc
@DisplayName("会话 API 集成测试")
class ChatIntegrationTest extends ScsPostgresRedisITBase {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private String customerToken;

    @BeforeEach
    void loginAndStubRouter() throws Exception {
        when(csIntentRouter.invokeAndGetOutput(anyString(), any(RunnableConfig.class)))
                .thenReturn(Optional.empty());

        MvcResult login = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"customer1\",\"password\":\"customer123\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andReturn();
        JsonNode root = objectMapper.readTree(login.getResponse().getContentAsString());
        customerToken = root.path("data").path("accessToken").asText();
    }

    @Test
    @DisplayName("客户 ask 返回 code=0（路由已 Mock）")
    void askReturnsOk() throws Exception {
        mockMvc.perform(post("/api/chat/ask")
                        .header("Authorization", "Bearer " + customerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"conversationId":"it-chat-001","question":"收到商品后多久可以申请退货？"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));
    }
}
