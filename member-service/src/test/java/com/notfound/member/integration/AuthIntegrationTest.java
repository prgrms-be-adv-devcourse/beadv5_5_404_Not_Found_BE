package com.notfound.member.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class AuthIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private static String accessToken;
    private static String refreshToken;

    // ===== 회원가입 =====

    @Test
    @Order(1)
    @DisplayName("회원가입 성공")
    void register_success() throws Exception {
        String body = """
                {
                    "email": "test@example.com",
                    "password": "Test1234!@",
                    "name": "테스트유저",
                    "phone": "01012345678"
                }
                """;

        MvcResult result = mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value(201))
                .andExpect(jsonPath("$.code").value("MEMBER_REGISTER_SUCCESS"))
                .andExpect(jsonPath("$.data.memberId").exists())
                .andExpect(jsonPath("$.data.accessToken").exists())
                .andExpect(jsonPath("$.data.refreshToken").exists())
                .andReturn();

        JsonNode json = objectMapper.readTree(result.getResponse().getContentAsString());
        accessToken = json.get("data").get("accessToken").asText();
        refreshToken = json.get("data").get("refreshToken").asText();
    }

    @Test
    @Order(2)
    @DisplayName("회원가입 실패 - 중복 이메일")
    void register_duplicateEmail() throws Exception {
        String body = """
                {
                    "email": "test@example.com",
                    "password": "Test1234!@",
                    "name": "중복유저",
                    "phone": "01099999999"
                }
                """;

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("EMAIL_ALREADY_EXISTS"));
    }

    @Test
    @Order(3)
    @DisplayName("회원가입 실패 - 비밀번호 복잡도 미충족")
    void register_validation_weakPassword() throws Exception {
        String body = """
                {
                    "email": "weak@example.com",
                    "password": "12345678",
                    "name": "약한비번",
                    "phone": "01011111111"
                }
                """;

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_INPUT_VALUE"))
                .andExpect(jsonPath("$.data.password").exists());
    }

    @Test
    @Order(4)
    @DisplayName("회원가입 실패 - 이메일 누락")
    void register_validation_missingEmail() throws Exception {
        String body = """
                {
                    "email": "",
                    "password": "Test1234!@",
                    "name": "이메일없음",
                    "phone": "01022222222"
                }
                """;

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_INPUT_VALUE"))
                .andExpect(jsonPath("$.data.email").exists());
    }

    // ===== 로그인 =====

    @Test
    @Order(5)
    @DisplayName("로그인 성공")
    void login_success() throws Exception {
        String body = """
                {
                    "email": "test@example.com",
                    "password": "Test1234!@"
                }
                """;

        MvcResult result = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("LOGIN_SUCCESS"))
                .andExpect(jsonPath("$.data.accessToken").exists())
                .andExpect(jsonPath("$.data.refreshToken").exists())
                .andReturn();

        JsonNode json = objectMapper.readTree(result.getResponse().getContentAsString());
        accessToken = json.get("data").get("accessToken").asText();
        refreshToken = json.get("data").get("refreshToken").asText();
    }

    @Test
    @Order(6)
    @DisplayName("로그인 실패 - 존재하지 않는 이메일")
    void login_notFoundEmail() throws Exception {
        String body = """
                {
                    "email": "nobody@example.com",
                    "password": "Test1234!@"
                }
                """;

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("INVALID_CREDENTIALS"));
    }

    @Test
    @Order(7)
    @DisplayName("로그인 실패 - 비밀번호 불일치")
    void login_wrongPassword() throws Exception {
        String body = """
                {
                    "email": "test@example.com",
                    "password": "WrongPass1!@"
                }
                """;

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("INVALID_CREDENTIALS"));
    }

    // ===== 토큰 갱신 =====

    @Test
    @Order(8)
    @DisplayName("토큰 갱신 성공")
    void refresh_success() throws Exception {
        String body = """
                { "refreshToken": "%s" }
                """.formatted(refreshToken);

        MvcResult result = mockMvc.perform(post("/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("TOKEN_REFRESH_SUCCESS"))
                .andExpect(jsonPath("$.data.accessToken").exists())
                .andExpect(jsonPath("$.data.refreshToken").exists())
                .andReturn();

        JsonNode json = objectMapper.readTree(result.getResponse().getContentAsString());
        accessToken = json.get("data").get("accessToken").asText();
        refreshToken = json.get("data").get("refreshToken").asText();
    }

    @Test
    @Order(9)
    @DisplayName("토큰 갱신 실패 - 폐기된 토큰 재사용 → 탈취 감지")
    void refresh_reuse_detectedAsHijack() throws Exception {
        // 새로 로그인
        String loginBody = """
                {
                    "email": "test@example.com",
                    "password": "Test1234!@"
                }
                """;

        MvcResult loginResult = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginBody))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode loginJson = objectMapper.readTree(loginResult.getResponse().getContentAsString());
        String firstRefreshToken = loginJson.get("data").get("refreshToken").asText();

        // 첫 번째 갱신 (정상)
        String refreshBody = """
                { "refreshToken": "%s" }
                """.formatted(firstRefreshToken);

        mockMvc.perform(post("/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(refreshBody))
                .andExpect(status().isOk());

        // 같은 토큰으로 재시도 (탈취 감지)
        mockMvc.perform(post("/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(refreshBody))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("MEMBER_TOKEN_HIJACKED"));
    }

    // ===== 로그아웃 =====

    @Test
    @Order(10)
    @DisplayName("로그아웃 성공")
    void logout_success() throws Exception {
        // 새로 로그인
        String loginBody = """
                {
                    "email": "test@example.com",
                    "password": "Test1234!@"
                }
                """;

        MvcResult loginResult = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginBody))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode loginJson = objectMapper.readTree(loginResult.getResponse().getContentAsString());
        String logoutAccessToken = loginJson.get("data").get("accessToken").asText();
        String logoutRefreshToken = loginJson.get("data").get("refreshToken").asText();

        String logoutBody = """
                { "refreshToken": "%s" }
                """.formatted(logoutRefreshToken);

        JsonNode memberData = loginJson.get("data");
        String logoutUserId = memberData.get("memberId").asText();

        mockMvc.perform(post("/auth/logout")
                        .header("Authorization", "Bearer " + logoutAccessToken)
                        .header("X-User-Id", logoutUserId)
                        .header("X-Role", "USER")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(logoutBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("LOGOUT_SUCCESS"));
    }
}
