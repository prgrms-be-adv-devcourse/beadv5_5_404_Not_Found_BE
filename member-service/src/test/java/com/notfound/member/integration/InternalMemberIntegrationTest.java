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
class InternalMemberIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private static final String INTERNAL_SECRET = "test-internal-secret";

    private String memberId;

    @BeforeAll
    void setUp() throws Exception {
        // 회원가입
        String registerBody = """
                {
                    "email": "internal-member-test@example.com",
                    "password": "Test1234!@",
                    "name": "내부API테스트",
                    "phone": "010-5555-6666"
                }
                """;

        MvcResult result = mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerBody))
                .andExpect(status().isCreated())
                .andReturn();

        JsonNode json = objectMapper.readTree(result.getResponse().getContentAsString());
        memberId = json.get("data").get("memberId").asText();

        // 배송지 등록
        String addressBody = """
                {
                    "label": "집",
                    "recipient": "내부테스트",
                    "phone": "010-7777-8888",
                    "zipcode": "54321",
                    "address1": "서울시 서초구 반포대로 1",
                    "address2": "202호"
                }
                """;

        mockMvc.perform(post("/member/addresses")
                        .header("X-User-Id", memberId)
                        .header("X-Role", "USER")
                        .header("X-Email-Verified", "true")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(addressBody))
                .andExpect(status().isCreated());
    }

    // ===== 배송지 목록 조회 =====

    @Test
    @Order(1)
    @DisplayName("[Internal] 배송지 목록 조회 성공")
    void getAddresses_success() throws Exception {
        mockMvc.perform(get("/internal/member/{memberId}/addresses", memberId)
                        .header("X-Internal-Secret", INTERNAL_SECRET))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("ADDRESS_LIST_FETCH_SUCCESS"))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].recipient").value("내부테스트"));
    }

    @Test
    @Order(2)
    @DisplayName("[Internal] 배송지 목록 조회 실패 - Internal Secret 누락")
    void getAddresses_noSecret() throws Exception {
        mockMvc.perform(get("/internal/member/{memberId}/addresses", memberId))
                .andDo(print())
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("ACCESS_DENIED"));
    }

    // ===== 회원 활성 상태 확인 =====

    @Test
    @Order(3)
    @DisplayName("[Internal] 회원 활성 상태 확인 성공")
    void isActiveMember_success() throws Exception {
        mockMvc.perform(get("/internal/member/{memberId}/active", memberId)
                        .header("X-Internal-Secret", INTERNAL_SECRET))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("MEMBER_ACTIVE_CHECK_SUCCESS"))
                .andExpect(jsonPath("$.data.active").value(true));
    }

    // ===== 예치금 잔액 조회 =====

    @Test
    @Order(4)
    @DisplayName("[Internal] 예치금 잔액 조회 성공")
    void getDepositBalance_success() throws Exception {
        mockMvc.perform(get("/internal/member/{memberId}/deposit", memberId)
                        .header("X-Internal-Secret", INTERNAL_SECRET))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("DEPOSIT_BALANCE_FOUND"))
                .andExpect(jsonPath("$.data.depositBalance").value(0));
    }

    // ===== 예치금 충전 =====

    @Test
    @Order(5)
    @DisplayName("[Internal] 예치금 충전 성공")
    void chargeDeposit_success() throws Exception {
        String body = """
                { "amount": 50000 }
                """;

        mockMvc.perform(post("/internal/member/{memberId}/deposit/charge", memberId)
                        .header("X-Internal-Secret", INTERNAL_SECRET)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("DEPOSIT_CHARGE_SUCCESS"))
                .andExpect(jsonPath("$.data.remainingBalance").value(50000));
    }

    // ===== 예치금 차감 =====

    @Test
    @Order(6)
    @DisplayName("[Internal] 예치금 차감 성공")
    void deductDeposit_success() throws Exception {
        String body = """
                { "amount": 10000 }
                """;

        mockMvc.perform(post("/internal/member/{memberId}/deposit/deduct", memberId)
                        .header("X-Internal-Secret", INTERNAL_SECRET)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("DEPOSIT_DEDUCT_SUCCESS"))
                .andExpect(jsonPath("$.data.remainingBalance").value(40000));
    }

    @Test
    @Order(7)
    @DisplayName("[Internal] 예치금 차감 실패 - 잔액 부족")
    void deductDeposit_insufficientBalance() throws Exception {
        String body = """
                { "amount": 999999 }
                """;

        mockMvc.perform(post("/internal/member/{memberId}/deposit/deduct", memberId)
                        .header("X-Internal-Secret", INTERNAL_SECRET)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }
}
