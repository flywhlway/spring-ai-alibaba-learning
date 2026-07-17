package com.flywhl.saa.smartcs;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.flywhl.saa.smartcs.support.ScsIntegrationTest;
import com.flywhl.saa.smartcs.support.ScsPostgresRedisITBase;

/**
 * 工单集成测试：创建工单 + 合法状态转移（无 API Key）。
 *
 * @author flywhl
 */
@ScsIntegrationTest
@AutoConfigureMockMvc
@DisplayName("工单 API 集成测试")
class TicketIntegrationTest extends ScsPostgresRedisITBase {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private String customerToken;
    private String agentToken;

    @BeforeEach
    void login() throws Exception {
        customerToken = loginToken("customer1", "customer123");
        agentToken = loginToken("agent1", "agent123");
    }

    @Test
    @DisplayName("创建工单后坐席可转移到 AI_PROCESSING")
    void createThenTransition() throws Exception {
        MvcResult create = mockMvc.perform(post("/api/tickets")
                        .header("Authorization", "Bearer " + customerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "conversationId": "it-ticket-001",
                                  "summary": "IT 测试建单",
                                  "priority": "HIGH"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.status").value("OPEN"))
                .andReturn();

        JsonNode ticket = objectMapper.readTree(create.getResponse().getContentAsString()).path("data");
        long ticketId = ticket.path("id").asLong();

        mockMvc.perform(patch("/api/tickets/" + ticketId + "/transition")
                        .header("Authorization", "Bearer " + agentToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"to\":\"AI_PROCESSING\",\"reason\":\"坐席开始处理\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.status").value("AI_PROCESSING"));
    }

    private String loginToken(String username, String password) throws Exception {
        MvcResult login = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"" + username + "\",\"password\":\"" + password + "\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andReturn();
        return objectMapper.readTree(login.getResponse().getContentAsString())
                .path("data").path("accessToken").asText();
    }
}
