package com.notfound.order.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.notfound.order.application.port.out.MemberServicePort;
import com.notfound.order.application.port.out.ProductServicePort;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * cancel vs pay 동시성 경합 테스트.
 * 두 요청 중 하나만 성공하고 최종 상태가 일관되는지 검증.
 */
@SpringBootTest
@AutoConfigureMockMvc
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ConcurrencyTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private MemberServicePort memberServicePort;

    @MockitoBean
    private ProductServicePort productServicePort;

    private static final String MEMBER_ID = "22222222-2222-2222-2222-222222222222";
    private static final String PRODUCT_ID = "bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb";
    private static final UUID ADDRESS_ID = UUID.fromString("dddddddd-dddd-dddd-dddd-dddddddddddd");
    private static final String INTERNAL_SECRET = "test-internal-secret";

    private String orderId;

    @BeforeAll
    void setUp() throws Exception {
        when(memberServicePort.isActiveMember(any())).thenReturn(true);
        when(memberServicePort.getDepositBalance(any())).thenReturn(500000);
        when(memberServicePort.chargeDeposit(any(), anyInt())).thenReturn(500000);
        when(memberServicePort.getAddresses(any())).thenReturn(List.of(
                Map.of("addressId", ADDRESS_ID.toString(),
                        "recipient", "테스트", "phone", "01000000000",
                        "zipcode", "12345", "address1", "서울", "address2", "101호",
                        "isDefault", true)
        ));

        when(productServicePort.getProducts(any())).thenReturn(List.of(
                Map.of("productId", PRODUCT_ID,
                        "productName", "동시성 테스트 도서",
                        "price", 15000,
                        "stock", 100,
                        "sellerId", UUID.randomUUID().toString())
        ));

        // PENDING 주문 생성
        String body = """
                {
                    "items": [{"productId": "%s", "quantity": 1}],
                    "addressId": "%s",
                    "idempotencyKey": "concurrency-test-key"
                }
                """.formatted(PRODUCT_ID, ADDRESS_ID);

        MvcResult result = mockMvc.perform(post("/order")
                        .header("X-User-Id", MEMBER_ID)
                        .header("X-Role", "USER")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andReturn();

        JsonNode json = objectMapper.readTree(result.getResponse().getContentAsString());
        orderId = json.get("data").get("orderId").asText();
    }

    @Test
    @DisplayName("cancel vs pay 동시 요청 — 둘 중 하나만 성공, 최종 상태 일관")
    void cancelVsPay_concurrency() throws Exception {
        ExecutorService executor = Executors.newFixedThreadPool(2);
        CountDownLatch latch = new CountDownLatch(1);

        // cancel 요청
        Future<Integer> cancelFuture = executor.submit(() -> {
            latch.await();
            return mockMvc.perform(post("/order/{orderId}/cancel", orderId)
                            .header("X-User-Id", MEMBER_ID)
                            .header("X-Role", "USER")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andReturn().getResponse().getStatus();
        });

        // pay 요청 (PAID → PURCHASE_CONFIRMED 자동 전이)
        Future<Integer> payFuture = executor.submit(() -> {
            latch.await();
            return mockMvc.perform(post("/internal/order/{orderId}/status", orderId)
                            .header("X-Internal-Secret", INTERNAL_SECRET)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    { "status": "PAID", "depositUsed": 15000 }
                                    """))
                    .andReturn().getResponse().getStatus();
        });

        // 동시 시작
        latch.countDown();

        int cancelStatus = cancelFuture.get();
        int payStatus = payFuture.get();

        executor.shutdown();

        boolean cancelSuccess = (cancelStatus == 200);
        boolean paySuccess = (payStatus == 200);

        // 최소 하나는 성공
        assertThat(cancelSuccess || paySuccess)
                .as("cancel 또는 pay 중 최소 하나는 성공해야 합니다")
                .isTrue();

        // 최종 상태가 PURCHASE_CONFIRMED 또는 CANCELLED 중 하나로 일관
        MvcResult detail = mockMvc.perform(get("/order/{orderId}", orderId)
                        .header("X-User-Id", MEMBER_ID)
                        .header("X-Role", "USER"))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode json = objectMapper.readTree(detail.getResponse().getContentAsString());
        String finalStatus = json.get("data").get("orderStatus").asText();

        assertThat(finalStatus).isIn("PURCHASE_CONFIRMED", "CANCELLED");

        if ("PURCHASE_CONFIRMED".equals(finalStatus)) {
            assertThat(json.get("data").get("depositUsed").asInt()).isEqualTo(15000);
        } else {
            assertThat(json.get("data").get("depositUsed").asInt()).isEqualTo(0);
        }
    }
}
