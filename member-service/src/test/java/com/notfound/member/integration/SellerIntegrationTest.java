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
class SellerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private String memberId;
    private String sellerId;

    @BeforeAll
    void setUp() throws Exception {
        String body = """
                {
                    "email": "seller-test@example.com",
                    "password": "Test1234!@",
                    "name": "판매자테스트",
                    "phone": "010-3333-4444"
                }
                """;

        MvcResult result = mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andReturn();

        JsonNode json = objectMapper.readTree(result.getResponse().getContentAsString());
        memberId = json.get("data").get("memberId").asText();
    }

    @Test
    @Order(1)
    @DisplayName("판매자 등록 성공")
    void registerSeller_success() throws Exception {
        String body = """
                {
                    "businessNumber": "123-45-67890",
                    "shopName": "테스트서점",
                    "bankCode": "004",
                    "bankAccount": "110-123-456789",
                    "accountHolder": "홍길동"
                }
                """;

        MvcResult result = mockMvc.perform(post("/member/seller")
                        .header("X-User-Id", memberId)
                        .header("X-Role", "USER")
                        .header("X-Email-Verified", "true")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value(201))
                .andExpect(jsonPath("$.code").value("SELLER_APPLY_SUCCESS"))
                .andExpect(jsonPath("$.data.sellerId").exists())
                .andExpect(jsonPath("$.data.memberId").value(memberId))
                .andExpect(jsonPath("$.data.shopName").value("테스트서점"))
                .andExpect(jsonPath("$.data.status").value("PENDING"))
                .andReturn();

        JsonNode json = objectMapper.readTree(result.getResponse().getContentAsString());
        sellerId = json.get("data").get("sellerId").asText();
    }

    @Test
    @Order(2)
    @DisplayName("판매자 등록 실패 - 이미 등록된 회원")
    void registerSeller_alreadyRegistered() throws Exception {
        String body = """
                {
                    "businessNumber": "999-99-99999",
                    "shopName": "중복서점",
                    "bankCode": "004",
                    "bankAccount": "110-999-999999",
                    "accountHolder": "김중복"
                }
                """;

        mockMvc.perform(post("/member/seller")
                        .header("X-User-Id", memberId)
                        .header("X-Role", "USER")
                        .header("X-Email-Verified", "true")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andDo(print())
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("SELLER_APPLICATION_ALREADY_EXISTS"));
    }

    @Test
    @Order(3)
    @DisplayName("판매자 등록 실패 - 필수값 누락")
    void registerSeller_validation() throws Exception {
        String body = """
                {
                    "businessNumber": "",
                    "shopName": "",
                    "bankCode": "",
                    "bankAccount": "",
                    "accountHolder": ""
                }
                """;

        mockMvc.perform(post("/member/seller")
                        .header("X-User-Id", memberId)
                        .header("X-Role", "USER")
                        .header("X-Email-Verified", "true")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_INPUT_VALUE"))
                .andExpect(jsonPath("$.data.businessNumber").exists())
                .andExpect(jsonPath("$.data.shopName").exists());
    }

    // ===== 판매자 프로필 조회 =====

    @Test
    @Order(4)
    @DisplayName("판매자 프로필 조회 성공")
    void getMySellerProfile_success() throws Exception {
        mockMvc.perform(get("/member/seller/{memberId}", memberId))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("SELLER_INFO_FETCH_SUCCESS"))
                .andExpect(jsonPath("$.data.shopName").value("테스트서점"))
                .andExpect(jsonPath("$.data.status").value("PENDING"));
    }

    // ===== 판매자 승인 =====

    @Test
    @Order(5)
    @DisplayName("판매자 승인 성공")
    void approveSeller_success() throws Exception {
        String body = """
                { "status": "APPROVED" }
                """;

        mockMvc.perform(patch("/member/admin/seller/{memberId}", memberId)
                        .header("X-User-Id", memberId)
                        .header("X-Role", "ADMIN")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("SELLER_STATUS_UPDATE_SUCCESS"))
                .andExpect(jsonPath("$.data.sellerStatus").value("APPROVED"));
    }

    @Test
    @Order(6)
    @DisplayName("판매자 승인 실패 - 이미 승인된 판매자")
    void approveSeller_notPending() throws Exception {
        String body = """
                { "status": "APPROVED" }
                """;

        mockMvc.perform(patch("/member/admin/seller/{memberId}", memberId)
                        .header("X-User-Id", memberId)
                        .header("X-Role", "ADMIN")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_SELLER_STATUS"));
    }

    @Test
    @Order(7)
    @DisplayName("판매자 승인 실패 - 존재하지 않는 판매자")
    void approveSeller_notFound() throws Exception {
        String body = """
                { "status": "APPROVED" }
                """;

        mockMvc.perform(patch("/member/admin/seller/{memberId}", "00000000-0000-0000-0000-000000000000")
                        .header("X-User-Id", memberId)
                        .header("X-Role", "ADMIN")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("SELLER_APPLICATION_NOT_FOUND"));
    }
}
