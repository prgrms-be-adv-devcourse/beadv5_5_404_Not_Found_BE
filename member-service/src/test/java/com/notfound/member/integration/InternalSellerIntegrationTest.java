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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class InternalSellerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private static final String INTERNAL_SECRET = "test-internal-secret";

    private String memberId;
    private String sellerId;

    @BeforeAll
    void setUp() throws Exception {
        // 회원가입
        String registerBody = """
                {
                    "email": "internal-seller-test@example.com",
                    "password": "Test1234!@",
                    "name": "내부판매자테스트",
                    "phone": "010-9999-0000"
                }
                """;

        MvcResult result = mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerBody))
                .andExpect(status().isCreated())
                .andReturn();

        JsonNode json = objectMapper.readTree(result.getResponse().getContentAsString());
        memberId = json.get("data").get("memberId").asText();

        // 판매자 등록
        String sellerBody = """
                {
                    "businessNumber": "555-55-55555",
                    "shopName": "내부테스트서점",
                    "bankCode": "004",
                    "bankAccount": "110-555-555555",
                    "accountHolder": "내부테스트"
                }
                """;

        MvcResult sellerResult = mockMvc.perform(post("/member/seller")
                        .header("X-User-Id", memberId)
                        .header("X-Role", "USER")
                        .header("X-Email-Verified", "true")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(sellerBody))
                .andExpect(status().isCreated())
                .andReturn();

        JsonNode sellerJson = objectMapper.readTree(sellerResult.getResponse().getContentAsString());
        sellerId = sellerJson.get("data").get("sellerId").asText();

        // 판매자 승인
        String approveBody = """
                { "status": "APPROVED" }
                """;

        mockMvc.perform(patch("/member/admin/seller/{memberId}", memberId)
                        .header("X-User-Id", memberId)
                        .header("X-Role", "ADMIN")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(approveBody))
                .andExpect(status().isOk());
    }

    // ===== 판매자 승인 상태 확인 =====

    @Test
    @Order(1)
    @DisplayName("[Internal] 판매자 승인 상태 확인 - 승인된 판매자")
    void isApprovedSeller_approved() throws Exception {
        mockMvc.perform(get("/internal/seller/{memberId}/approved", memberId)
                        .header("X-Internal-Secret", INTERNAL_SECRET))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("MEMBER_SELLER_STATUS_CHECK_SUCCESS"))
                .andExpect(jsonPath("$.data.approved").value(true));
    }

    @Test
    @Order(2)
    @DisplayName("[Internal] 판매자 승인 상태 확인 - 미등록 회원")
    void isApprovedSeller_notRegistered() throws Exception {
        mockMvc.perform(get("/internal/seller/00000000-0000-0000-0000-000000000000/approved")
                        .header("X-Internal-Secret", INTERNAL_SECRET))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.approved").value(false));
    }

    @Test
    @Order(3)
    @DisplayName("[Internal] 판매자 승인 상태 확인 실패 - Internal Secret 누락")
    void isApprovedSeller_noSecret() throws Exception {
        mockMvc.perform(get("/internal/seller/{memberId}/approved", memberId))
                .andDo(print())
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("ACCESS_DENIED"));
    }

    // ===== 판매자 계좌 정보 조회 =====

    @Test
    @Order(4)
    @DisplayName("[Internal] 판매자 계좌 정보 조회 성공")
    void getSellerAccount_success() throws Exception {
        mockMvc.perform(get("/internal/seller/{memberId}/account", memberId)
                        .header("X-Internal-Secret", INTERNAL_SECRET))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("SELLER_ACCOUNT_FETCH_SUCCESS"))
                .andExpect(jsonPath("$.data.bankCode").value("004"))
                .andExpect(jsonPath("$.data.bankAccount").value("110-555-555555"))
                .andExpect(jsonPath("$.data.accountHolder").value("내부테스트"));
    }

    @Test
    @Order(5)
    @DisplayName("[Internal] 판매자 계좌 정보 조회 실패 - 존재하지 않는 판매자")
    void getSellerAccount_notFound() throws Exception {
        mockMvc.perform(get("/internal/seller/00000000-0000-0000-0000-000000000000/account")
                        .header("X-Internal-Secret", INTERNAL_SECRET))
                .andDo(print())
                .andExpect(status().isNotFound());
    }
}
