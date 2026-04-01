package com.notfound.order.presentation.controller;

import com.notfound.order.application.port.in.ClearCartUseCase;
import com.notfound.order.application.port.in.GetInternalOrderUseCase;
import com.notfound.order.application.port.in.GetInternalOrderUseCase.InternalOrderDetail;
import com.notfound.order.application.port.in.UpdateOrderStatusUseCase;
import com.notfound.order.application.service.PendingOrderCleanupScheduler;
import com.notfound.order.domain.exception.OrderException;
import com.notfound.order.domain.model.Order;
import com.notfound.order.domain.model.OrderItem;
import com.notfound.order.domain.model.OrderItemStatus;
import com.notfound.order.domain.model.OrderStatus;
import com.notfound.order.infrastructure.security.InternalSecretFilter;
import com.notfound.order.infrastructure.security.RoleAuthorizationFilter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(InternalOrderController.class)
@Import({InternalSecretFilter.class, RoleAuthorizationFilter.class, GlobalExceptionHandler.class})
@TestPropertySource(properties = "internal.secret-key=test-internal-secret-key-for-testing")
class InternalOrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private GetInternalOrderUseCase getInternalOrderUseCase;

    @MockitoBean
    private UpdateOrderStatusUseCase updateOrderStatusUseCase;

    @MockitoBean
    private ClearCartUseCase clearCartUseCase;

    @MockitoBean
    private PendingOrderCleanupScheduler pendingOrderCleanupScheduler;

    private static final String INTERNAL_SECRET = "test-internal-secret-key-for-testing";
    private static final UUID ORDER_ID = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");
    private static final UUID PRODUCT_ID_1 = UUID.fromString("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb");
    private static final UUID PRODUCT_ID_2 = UUID.fromString("cccccccc-cccc-cccc-cccc-cccccccccccc");

    private Order createOrder(UUID orderId, int totalAmount) {
        return Order.builder()
                .id(orderId)
                .orderNumber("20260331-abc123def456")
                .memberId(UUID.randomUUID())
                .status(OrderStatus.PENDING)
                .totalAmount(totalAmount)
                .shippingFee(0)
                .depositUsed(0)
                .build();
    }

    private OrderItem createOrderItem(UUID orderId, UUID productId, int quantity) {
        return OrderItem.builder()
                .id(UUID.randomUUID())
                .orderId(orderId)
                .productId(productId)
                .sellerId(UUID.randomUUID())
                .productTitle("테스트 상품")
                .unitPrice(10000)
                .quantity(quantity)
                .subtotal(10000 * quantity)
                .status(OrderItemStatus.PAID)
                .build();
    }

    @Nested
    @DisplayName("GET /internal/order/{orderId} — 정상 요청")
    class HappyPath {

        @Test
        @DisplayName("유효한 X-Internal-Secret + 존재하는 orderId → 200 + 응답 body 검증")
        void getOrder_success() throws Exception {
            Order order = createOrder(ORDER_ID, 30000);
            List<OrderItem> items = List.of(
                    createOrderItem(ORDER_ID, PRODUCT_ID_1, 2),
                    createOrderItem(ORDER_ID, PRODUCT_ID_2, 1)
            );
            when(getInternalOrderUseCase.getOrder(ORDER_ID))
                    .thenReturn(new InternalOrderDetail(order, items));

            mockMvc.perform(get("/internal/order/{orderId}", ORDER_ID)
                            .header("X-Internal-Secret", INTERNAL_SECRET))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value(200))
                    .andExpect(jsonPath("$.code").value("ORDER_FOUND"))
                    .andExpect(jsonPath("$.data.orderId").value(ORDER_ID.toString()))
                    .andExpect(jsonPath("$.data.status").value("PENDING"))
                    .andExpect(jsonPath("$.data.totalAmount").value(30000))
                    .andExpect(jsonPath("$.data.items.length()").value(2))
                    .andExpect(jsonPath("$.data.items[0].productId").value(PRODUCT_ID_1.toString()))
                    .andExpect(jsonPath("$.data.items[0].quantity").value(2))
                    .andExpect(jsonPath("$.data.items[1].productId").value(PRODUCT_ID_2.toString()))
                    .andExpect(jsonPath("$.data.items[1].quantity").value(1));
        }

        @Test
        @DisplayName("CANCELLED 상태 주문 조회 → status: CANCELLED 반환")
        void getOrder_cancelledStatus() throws Exception {
            Order order = Order.builder()
                    .id(ORDER_ID)
                    .orderNumber("20260401-abc123def456")
                    .memberId(UUID.randomUUID())
                    .status(OrderStatus.CANCELLED)
                    .totalAmount(30000)
                    .shippingFee(0)
                    .depositUsed(0)
                    .build();
            when(getInternalOrderUseCase.getOrder(ORDER_ID))
                    .thenReturn(new InternalOrderDetail(order, List.of()));

            mockMvc.perform(get("/internal/order/{orderId}", ORDER_ID)
                            .header("X-Internal-Secret", INTERNAL_SECRET))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.status").value("CANCELLED"));
        }

        @Test
        @DisplayName("OrderItem이 0건인 주문 → 빈 items 리스트 반환")
        void getOrder_emptyItems() throws Exception {
            Order order = createOrder(ORDER_ID, 0);
            when(getInternalOrderUseCase.getOrder(ORDER_ID))
                    .thenReturn(new InternalOrderDetail(order, List.of()));

            mockMvc.perform(get("/internal/order/{orderId}", ORDER_ID)
                            .header("X-Internal-Secret", INTERNAL_SECRET))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.status").value("PENDING"))
                    .andExpect(jsonPath("$.data.items").isArray())
                    .andExpect(jsonPath("$.data.items.length()").value(0));
        }
    }

    @Nested
    @DisplayName("GET /internal/order/{orderId} — InternalSecretFilter 인증")
    class SecretValidation {

        @Test
        @DisplayName("X-Internal-Secret 헤더 누락 → 403")
        void getOrder_noSecret() throws Exception {
            mockMvc.perform(get("/internal/order/{orderId}", ORDER_ID))
                    .andDo(print())
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.status").value(403))
                    .andExpect(jsonPath("$.code").value("ACCESS_DENIED"));
        }

        @Test
        @DisplayName("X-Internal-Secret 값 불일치 → 403")
        void getOrder_wrongSecret() throws Exception {
            mockMvc.perform(get("/internal/order/{orderId}", ORDER_ID)
                            .header("X-Internal-Secret", "wrong-secret"))
                    .andDo(print())
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.code").value("ACCESS_DENIED"));
        }
    }

    @Nested
    @DisplayName("GET /internal/order/{orderId} — 에러 케이스")
    class ErrorCases {

        @Test
        @DisplayName("존재하지 않는 orderId → 404")
        void getOrder_notFound() throws Exception {
            UUID nonExistentId = UUID.fromString("00000000-0000-0000-0000-000000000000");
            when(getInternalOrderUseCase.getOrder(nonExistentId))
                    .thenThrow(OrderException.orderNotFound());

            mockMvc.perform(get("/internal/order/{orderId}", nonExistentId)
                            .header("X-Internal-Secret", INTERNAL_SECRET))
                    .andDo(print())
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.code").value("ORDER_NOT_FOUND"));
        }

        @Test
        @DisplayName("orderId 형식 잘못됨 (UUID 아닌 값) → 400")
        void getOrder_invalidUuid() throws Exception {
            mockMvc.perform(get("/internal/order/{orderId}", "not-a-uuid")
                            .header("X-Internal-Secret", INTERNAL_SECRET))
                    .andDo(print())
                    .andExpect(status().isBadRequest());
        }
    }
}
