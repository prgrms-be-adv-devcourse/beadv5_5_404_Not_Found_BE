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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class AddressIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private String memberId;
    private String addressId;

    @BeforeAll
    void setUp() throws Exception {
        // 회원가입하여 memberId 확보
        String body = """
                {
                    "email": "address-test@example.com",
                    "password": "Test1234!@",
                    "name": "배송지테스트",
                    "phone": "010-0000-0000"
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
    @DisplayName("배송지 등록 성공")
    void createAddress_success() throws Exception {
        String body = """
                {
                    "recipient": "홍길동",
                    "phone": "010-1111-2222",
                    "zipcode": "12345",
                    "address1": "서울시 강남구 테헤란로 1",
                    "address2": "101호",
                    "isDefault": true
                }
                """;

        MvcResult result = mockMvc.perform(post("/member/address")
                        .header("X-User-Id", memberId)
                        .header("X-Role", "USER")
                        .header("X-Email-Verified", "true")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value(201))
                .andExpect(jsonPath("$.code").value("ADDRESS_CREATE_SUCCESS"))
                .andExpect(jsonPath("$.data.addressId").exists())
                .andExpect(jsonPath("$.data.recipient").value("홍길동"))
                .andExpect(jsonPath("$.data.zipcode").value("12345"))
                .andReturn();

        JsonNode json = objectMapper.readTree(result.getResponse().getContentAsString());
        addressId = json.get("data").get("addressId").asText();
    }

    @Test
    @Order(2)
    @DisplayName("배송지 등록 실패 - 필수값 누락")
    void createAddress_validation() throws Exception {
        String body = """
                {
                    "recipient": "",
                    "phone": "",
                    "zipcode": "",
                    "address1": ""
                }
                """;

        mockMvc.perform(post("/member/address")
                        .header("X-User-Id", memberId)
                        .header("X-Role", "USER")
                        .header("X-Email-Verified", "true")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_INPUT_VALUE"))
                .andExpect(jsonPath("$.data.recipient").exists())
                .andExpect(jsonPath("$.data.phone").exists())
                .andExpect(jsonPath("$.data.zipcode").exists())
                .andExpect(jsonPath("$.data.address1").exists());
    }

    @Test
    @Order(3)
    @DisplayName("배송지 삭제 성공 (소프트 삭제)")
    void deleteAddress_success() throws Exception {
        mockMvc.perform(delete("/member/address/{addressId}", addressId)
                        .header("X-User-Id", memberId)
                        .header("X-Role", "USER")
                        .header("X-Email-Verified", "true"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("ADDRESS_DELETE_SUCCESS"));
    }

    @Test
    @Order(4)
    @DisplayName("배송지 삭제 실패 - 존재하지 않는 배송지")
    void deleteAddress_notFound() throws Exception {
        mockMvc.perform(delete("/member/address/00000000-0000-0000-0000-000000000000")
                        .header("X-User-Id", memberId)
                        .header("X-Role", "USER")
                        .header("X-Email-Verified", "true"))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("ADDRESS_NOT_FOUND"));
    }
}
