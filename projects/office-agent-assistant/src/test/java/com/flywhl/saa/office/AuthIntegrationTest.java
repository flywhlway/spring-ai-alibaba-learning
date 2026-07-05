package com.flywhl.saa.office;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.test.web.servlet.MockMvc;

import com.alibaba.cloud.ai.graph.agent.ReactAgent;
import com.alibaba.cloud.ai.graph.agent.flow.agent.LlmRoutingAgent;
import com.alibaba.cloud.ai.graph.agent.flow.agent.SequentialAgent;
import com.flywhl.saa.office.support.OfficeIntegrationTest;
import com.flywhl.saa.office.support.OfficeMySqlITBase;

/**
 * 认证 API MySQL 集成测试。
 *
 * @author flywhl
 */
@OfficeIntegrationTest
@AutoConfigureMockMvc
@DisplayName("认证 API 集成测试")
class AuthIntegrationTest extends OfficeMySqlITBase {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private JwtEncoder jwtEncoder;

    @MockBean
    private VectorStore vectorStore;

    @MockBean(name = "officeAssistantAgent")
    private ReactAgent officeAssistantAgent;

    @MockBean(name = "approvalSequentialPipeline")
    private SequentialAgent approvalSequentialPipeline;

    @MockBean(name = "approvalRoutingAgent")
    private LlmRoutingAgent approvalRoutingAgent;

    @BeforeEach
    void stubJwtEncoder() {
        when(jwtEncoder.encode(any(JwtEncoderParameters.class)))
                .thenReturn(Jwt.withTokenValue("it-test-jwt-token")
                        .header("alg", "none")
                        .claim("sub", "admin")
                        .claim("uid", 1L)
                        .claim("role", "ADMIN")
                        .build());
    }

    @Test
    @DisplayName("admin 登录返回 code=0 与 accessToken")
    void loginAdminReturnsToken() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"admin\",\"password\":\"admin123\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.accessToken").value("it-test-jwt-token"));
    }
}
