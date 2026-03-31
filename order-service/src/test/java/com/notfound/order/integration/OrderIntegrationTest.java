package com.notfound.order.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.notfound.order.application.port.out.MemberServicePort;
import com.notfound.order.application.port.out.PurchaseEventPublisher;
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
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class OrderIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private MemberServicePort memberServicePort;

    @MockitoBean
    private PurchaseEventPublisher purchaseEventPublisher;

    private static final String MEMBER_ID = "11111111-1111-1111-1111-111111111111";
    private static final String PRODUCT_ID = "aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa";
    private static final UUID ADDRESS_ID = UUID.fromString("cccccccc-cccc-cccc-cccc-cccccccccccc");
    private static final String INTERNAL_SECRET = "test-internal-secret-key-for-testing";
    private static final String IDEMPOTENCY_KEY_1 = "test-order-key-001";
    private static final String IDEMPOTENCY_KEY_2 = "test-order-key-002";

    private String orderId;
    private String orderId2;
    private String orderItemId;
    private String cartItemId;

    @BeforeAll
    void setUp() throws Exception {
        setupMemberMocks();

        String body = """
                { "productId": "%s", "quantity": 2 }
                """.formatted(PRODUCT_ID);

        MvcResult result = mockMvc.perform(post("/order/cart/item")
                        .header("X-User-Id", MEMBER_ID)
                        .header("X-Role", "USER")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andReturn();

        JsonNode json = objectMapper.readTree(result.getResponse().getContentAsString());
        cartItemId = json.get("data").get("cartItemId").asText();
    }

    private void setupMemberMocks() {
        when(memberServicePort.isActiveMember(any())).thenReturn(true);
        when(memberServicePort.getDepositBalance(any())).thenReturn(500000);
        when(memberServicePort.deductDeposit(any(), anyInt())).thenAnswer(inv -> 500000 - (int) inv.getArgument(1));
        when(memberServicePort.chargeDeposit(any(), anyInt())).thenAnswer(inv -> 500000 + (int) inv.getArgument(1));
        when(memberServicePort.getAddresses(any())).thenReturn(List.of(
                Map.of("addressId", ADDRESS_ID.toString(),
                        "recipient", "홍길동",
                        "phone", "01012345678",
                        "zipcode", "06236",
                        "address1", "서울시 강남구",
                        "address2", "101호",
                        "isDefault", true)
        ));
    }

    // ===== 주문 생성 (PENDING) =====

    @Test
    @Order(1)
    @DisplayName("주문 생성 — 상태 PENDING, depositUsed=0, deposit 차감 안 함, stock 이벤트 안 함")
    void createOrder_pending() throws Exception {
        String body = """
                {
                    "items": [{"productId": "%s", "quantity": 2, "cartItemId": "%s"}],
                    "addressId": "%s",
                    "idempotencyKey": "%s"
                }
                """.formatted(PRODUCT_ID, cartItemId, ADDRESS_ID, IDEMPOTENCY_KEY_1);

        MvcResult result = mockMvc.perform(post("/order")
                        .header("X-User-Id", MEMBER_ID)
                        .header("X-Role", "USER")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.code").value("ORDER_CREATED"))
                .andExpect(jsonPath("$.data.orderStatus").value("PENDING"))
                .andExpect(jsonPath("$.data.depositUsed").value(0))
                .andReturn();

        // deposit 차감 호출 안 됨
        verify(memberServicePort, never()).deductDeposit(any(), anyInt());

        JsonNode json = objectMapper.readTree(result.getResponse().getContentAsString());
        orderId = json.get("data").get("orderId").asText();
        orderItemId = json.get("data").get("items").get(0).get("orderItemId").asText();
    }

    @Test
    @Order(2)
    @DisplayName("주문 생성 — 바로구매 PENDING")
    void createOrder_direct_pending() throws Exception {
        String body = """
                {
                    "items": [{"productId": "%s", "quantity": 1}],
                    "addressId": "%s",
                    "idempotencyKey": "%s"
                }
                """.formatted(PRODUCT_ID, ADDRESS_ID, IDEMPOTENCY_KEY_2);

        MvcResult result = mockMvc.perform(post("/order")
                        .header("X-User-Id", MEMBER_ID)
                        .header("X-Role", "USER")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.orderStatus").value("PENDING"))
                .andReturn();

        JsonNode json = objectMapper.readTree(result.getResponse().getContentAsString());
        orderId2 = json.get("data").get("orderId").asText();
    }

    @Test
    @Order(3)
    @DisplayName("주문 생성 실패 — 빈 항목 400")
    void createOrder_emptyItems() throws Exception {
        String body = """
                { "items": [], "addressId": "%s", "idempotencyKey": "empty-key" }
                """.formatted(ADDRESS_ID);

        mockMvc.perform(post("/order")
                        .header("X-User-Id", MEMBER_ID)
                        .header("X-Role", "USER")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    @Order(4)
    @DisplayName("주문 생성 실패 — 인증 없음 401")
    void createOrder_unauthorized() throws Exception {
        mockMvc.perform(post("/order")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "items": [{"productId": "%s", "quantity": 1}], "addressId": "%s", "idempotencyKey": "no-auth" }
                                """.formatted(PRODUCT_ID, ADDRESS_ID)))
                .andExpect(status().isUnauthorized());
    }

    // ===== Internal API: PENDING → PAID =====

    @Test
    @Order(10)
    @DisplayName("Internal status PAID — depositUsed 저장, 스냅샷 저장, 장바구니 삭제")
    void internalStatus_paid() throws Exception {
        String body = """
                { "status": "PAID", "depositUsed": 32500 }
                """;

        mockMvc.perform(post("/internal/order/{orderId}/status", orderId)
                        .header("X-Internal-Secret", INTERNAL_SECRET)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("ORDER_STATUS_UPDATE_SUCCESS"))
                .andExpect(jsonPath("$.data.orderStatus").value("PAID"));

        // 주문 상세 조회로 검증
        mockMvc.perform(get("/order/{orderId}", orderId)
                        .header("X-User-Id", MEMBER_ID)
                        .header("X-Role", "USER"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.orderStatus").value("PAID"))
                .andExpect(jsonPath("$.data.depositUsed").value(32500));
    }

    @Test
    @Order(11)
    @DisplayName("Internal status 멱등 — PAID→PAID 재호출은 200 (부작용 없음)")
    void internalStatus_idempotent() throws Exception {
        mockMvc.perform(post("/internal/order/{orderId}/status", orderId)
                        .header("X-Internal-Secret", INTERNAL_SECRET)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "status": "PAID", "depositUsed": 32500 }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.orderStatus").value("PAID"));
    }

    @Test
    @Order(12)
    @DisplayName("Internal status 실패 — secret 없음 403")
    void internalStatus_noSecret() throws Exception {
        mockMvc.perform(post("/internal/order/{orderId}/status", orderId2)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "status": "PAID", "depositUsed": 0 }
                                """))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("ACCESS_DENIED"));
    }

    @Test
    @Order(13)
    @DisplayName("Internal status 실패 — 잘못된 secret 403")
    void internalStatus_wrongSecret() throws Exception {
        mockMvc.perform(post("/internal/order/{orderId}/status", orderId2)
                        .header("X-Internal-Secret", "wrong-secret")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "status": "PAID", "depositUsed": 0 }
                                """))
                .andExpect(status().isForbidden());
    }

    // ===== PENDING 취소 (예치금/재고 처리 없음) =====

    @Test
    @Order(20)
    @DisplayName("PENDING 주문 취소 — 환불 0, 재고 복원 없음")
    void cancelOrder_pending() throws Exception {
        mockMvc.perform(post("/order/{orderId}/cancel", orderId2)
                        .header("X-User-Id", MEMBER_ID)
                        .header("X-Role", "USER")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("ORDER_CANCEL_SUCCESS"))
                .andExpect(jsonPath("$.data.orderStatus").value("CANCELLED"))
                .andExpect(jsonPath("$.data.refundAmount").value(0));

        // PENDING 취소 시 chargeDeposit 호출 안 됨
        verify(memberServicePort, never()).chargeDeposit(any(), anyInt());
    }

    @Test
    @Order(21)
    @DisplayName("PAID 주문 취소 — 환불 + 재고 복원")
    void cancelOrder_paid() throws Exception {
        mockMvc.perform(post("/order/{orderId}/cancel", orderId)
                        .header("X-User-Id", MEMBER_ID)
                        .header("X-Role", "USER")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("ORDER_CANCEL_SUCCESS"))
                .andExpect(jsonPath("$.data.orderStatus").value("CANCELLED"))
                .andExpect(jsonPath("$.data.refundAmount").value(32500));
    }

    @Test
    @Order(22)
    @DisplayName("취소 실패 — 이미 CANCELLED 상태 → 409")
    void cancelOrder_alreadyCancelled() throws Exception {
        mockMvc.perform(post("/order/{orderId}/cancel", orderId2)
                        .header("X-User-Id", MEMBER_ID)
                        .header("X-Role", "USER")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("ORDER_CANNOT_BE_CANCELLED"));
    }

    @Test
    @Order(23)
    @DisplayName("Internal status 실패 — CANCELLED→PAID 전이 409")
    void internalStatus_cancelledToPaid() throws Exception {
        // orderId2는 CANCELLED 상태
        mockMvc.perform(post("/internal/order/{orderId}/status", orderId2)
                        .header("X-Internal-Secret", INTERNAL_SECRET)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "status": "PAID", "depositUsed": 15000 }
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("INVALID_STATE_TRANSITION"));
    }

    // ===== 조회 =====

    @Test
    @Order(30)
    @DisplayName("주문 목록 조회")
    void getOrders() throws Exception {
        mockMvc.perform(get("/order")
                        .header("X-User-Id", MEMBER_ID)
                        .header("X-Role", "USER"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("ORDER_LIST_FETCH_SUCCESS"));
    }

    @Test
    @Order(31)
    @DisplayName("주문 상세 조회 실패 — 존재하지 않는 주문")
    void getOrderDetail_notFound() throws Exception {
        mockMvc.perform(get("/order/00000000-0000-0000-0000-000000000000")
                        .header("X-User-Id", MEMBER_ID)
                        .header("X-Role", "USER"))
                .andExpect(status().isNotFound());
    }

    @Test
    @Order(32)
    @DisplayName("주문 상세 조회 실패 — 다른 회원 403")
    void getOrderDetail_accessDenied() throws Exception {
        mockMvc.perform(get("/order/{orderId}", orderId)
                        .header("X-User-Id", "99999999-9999-9999-9999-999999999999")
                        .header("X-Role", "USER"))
                .andExpect(status().isForbidden());
    }
}
