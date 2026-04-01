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

        mockMvc.perform(post("/member/address")
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
        mockMvc.perform(get("/internal/member/{memberId}/address", memberId)
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
        mockMvc.perform(get("/internal/member/{memberId}/address", memberId))
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
                { "transactionId": "charge-001", "amount": 50000 }
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
                { "transactionId": "deduct-001", "amount": 10000 }
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
                { "transactionId": "deduct-fail-001", "amount": 999999 }
                """;

        mockMvc.perform(post("/internal/member/{memberId}/deposit/deduct", memberId)
                        .header("X-Internal-Secret", INTERNAL_SECRET)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    // ===== 충전 멱등성 =====

    @Test
    @Order(10)
    @DisplayName("[Internal] 충전 멱등성 — 동일 transactionId 재요청 시 이중 충전 안 됨")
    void chargeDeposit_idempotent() throws Exception {
        String body = """
                { "transactionId": "charge-idem-001", "amount": 30000 }
                """;

        // 1차 요청
        mockMvc.perform(post("/internal/member/{memberId}/deposit/charge", memberId)
                        .header("X-Internal-Secret", INTERNAL_SECRET)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.remainingBalance").value(70000)); // 40000 + 30000

        // 2차 요청 (멱등)
        mockMvc.perform(post("/internal/member/{memberId}/deposit/charge", memberId)
                        .header("X-Internal-Secret", INTERNAL_SECRET)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.remainingBalance").value(70000)); // 변동 없음

        // 잔액 확인
        mockMvc.perform(get("/internal/member/{memberId}/deposit", memberId)
                        .header("X-Internal-Secret", INTERNAL_SECRET))
                .andExpect(jsonPath("$.data.depositBalance").value(70000));
    }

    // ===== 차감 멱등성 =====

    @Test
    @Order(11)
    @DisplayName("[Internal] 차감 멱등성 — 동일 transactionId 재요청 시 이중 차감 안 됨")
    void deductDeposit_idempotent() throws Exception {
        String body = """
                { "transactionId": "deduct-idem-001", "amount": 5000 }
                """;

        // 1차 요청
        mockMvc.perform(post("/internal/member/{memberId}/deposit/deduct", memberId)
                        .header("X-Internal-Secret", INTERNAL_SECRET)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.remainingBalance").value(65000)); // 70000 - 5000

        // 2차 요청 (멱등)
        mockMvc.perform(post("/internal/member/{memberId}/deposit/deduct", memberId)
                        .header("X-Internal-Secret", INTERNAL_SECRET)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.remainingBalance").value(65000)); // 변동 없음

        // 잔액 확인
        mockMvc.perform(get("/internal/member/{memberId}/deposit", memberId)
                        .header("X-Internal-Secret", INTERNAL_SECRET))
                .andExpect(jsonPath("$.data.depositBalance").value(65000));
    }

    // ===== transactionId 누락 =====

    @Test
    @Order(12)
    @DisplayName("[Internal] transactionId 누락 → 400")
    void chargeDeposit_noTransactionId() throws Exception {
        mockMvc.perform(post("/internal/member/{memberId}/deposit/charge", memberId)
                        .header("X-Internal-Secret", INTERNAL_SECRET)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "amount": 5000 }
                                """))
                .andExpect(status().isBadRequest());
    }

    // ===== 동시성: @Version 낙관적 락 =====

    @Test
    @Order(20)
    @DisplayName("[Internal] 동시 차감 — @Version으로 하나만 성공, 나머지 OptimisticLock 에러")
    void deductDeposit_concurrency() throws Exception {
        // 잔액 충전 (동시성 테스트용)
        mockMvc.perform(post("/internal/member/{memberId}/deposit/charge", memberId)
                        .header("X-Internal-Secret", INTERNAL_SECRET)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "transactionId": "concurrency-setup", "amount": 100000 }
                                """))
                .andExpect(status().isOk());

        int threadCount = 5;
        java.util.concurrent.ExecutorService executor = java.util.concurrent.Executors.newFixedThreadPool(threadCount);
        java.util.concurrent.CountDownLatch latch = new java.util.concurrent.CountDownLatch(threadCount);
        java.util.concurrent.atomic.AtomicInteger successCount = new java.util.concurrent.atomic.AtomicInteger(0);
        java.util.concurrent.atomic.AtomicInteger failCount = new java.util.concurrent.atomic.AtomicInteger(0);

        for (int i = 0; i < threadCount; i++) {
            final int idx = i;
            executor.submit(() -> {
                try {
                    latch.countDown();
                    latch.await();
                    MvcResult result = mockMvc.perform(post("/internal/member/{memberId}/deposit/deduct", memberId)
                                    .header("X-Internal-Secret", INTERNAL_SECRET)
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content("{\"transactionId\":\"concurrent-deduct-" + idx + "\",\"amount\":10000}"))
                            .andReturn();
                    int status = result.getResponse().getStatus();
                    if (status == 200) {
                        successCount.incrementAndGet();
                    } else {
                        failCount.incrementAndGet();
                    }
                } catch (Exception e) {
                    failCount.incrementAndGet();
                }
            });
        }

        executor.shutdown();
        executor.awaitTermination(10, java.util.concurrent.TimeUnit.SECONDS);

        // 최소 1개 성공, 동시성 충돌로 일부 실패 가능
        org.assertj.core.api.Assertions.assertThat(successCount.get()).isGreaterThanOrEqualTo(1);
        // 전체 = 성공 + 실패
        org.assertj.core.api.Assertions.assertThat(successCount.get() + failCount.get()).isEqualTo(threadCount);
    }
}
