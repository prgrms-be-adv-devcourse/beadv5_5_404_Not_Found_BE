package com.notfound.member.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class InternalTokenBlacklistIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private static final String INTERNAL_SECRET = "test-internal-secret";

    private String blacklistedJti;

    @BeforeAll
    void setUp() throws Exception {
        // 회원가입
        String registerBody = """
                {
                    "email": "token-blacklist-test@example.com",
                    "password": "Test1234!@",
                    "name": "블랙리스트테스트",
                    "phone": "010-1111-0000"
                }
                """;

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerBody))
                .andExpect(status().isCreated());

        // 로그인
        String loginBody = """
                {
                    "email": "token-blacklist-test@example.com",
                    "password": "Test1234!@"
                }
                """;

        MvcResult loginResult = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginBody))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode loginJson = objectMapper.readTree(loginResult.getResponse().getContentAsString());
        String accessToken = loginJson.get("data").get("accessToken").asText();
        String refreshToken = loginJson.get("data").get("refreshToken").asText();
        String userId = loginJson.get("data").get("memberId").asText();

        // JWT에서 jti 추출 (payload 파싱)
        String[] parts = accessToken.split("\\.");
        String payload = new String(java.util.Base64.getUrlDecoder().decode(parts[1]));
        JsonNode payloadJson = objectMapper.readTree(payload);
        blacklistedJti = payloadJson.get("jti").asText();

        // 로그아웃 → 토큰 블랙리스트 등록
        String logoutBody = """
                { "refreshToken": "%s" }
                """.formatted(refreshToken);

        mockMvc.perform(post("/auth/logout")
                        .header("Authorization", "Bearer " + accessToken)
                        .header("X-User-Id", userId)
                        .header("X-Role", "USER")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(logoutBody))
                .andExpect(status().isOk());
    }

    @Test
    @Order(1)
    @DisplayName("[Internal] 블랙리스트 확인 - 블랙리스트된 토큰")
    void isBlacklisted_true() throws Exception {
        mockMvc.perform(get("/internal/token-blacklist/{jti}", blacklistedJti)
                        .header("X-Internal-Secret", INTERNAL_SECRET))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string("true"));
    }

    @Test
    @Order(2)
    @DisplayName("[Internal] 블랙리스트 확인 - 블랙리스트되지 않은 토큰")
    void isBlacklisted_false() throws Exception {
        mockMvc.perform(get("/internal/token-blacklist/{jti}", "non-existent-jti")
                        .header("X-Internal-Secret", INTERNAL_SECRET))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string("false"));
    }

    @Test
    @Order(3)
    @DisplayName("[Internal] 블랙리스트 확인 실패 - Internal Secret 누락")
    void isBlacklisted_noSecret() throws Exception {
        mockMvc.perform(get("/internal/token-blacklist/{jti}", blacklistedJti))
                .andDo(print())
                .andExpect(status().isForbidden());
    }
}
