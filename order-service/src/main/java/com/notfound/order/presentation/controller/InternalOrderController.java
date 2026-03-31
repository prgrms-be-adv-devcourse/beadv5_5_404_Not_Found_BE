package com.notfound.order.presentation.controller;

import com.notfound.order.application.port.in.ClearCartUseCase;
import com.notfound.order.application.port.in.GetInternalOrderUseCase;
import com.notfound.order.application.port.in.UpdateOrderStatusUseCase;
import com.notfound.order.application.service.PendingOrderCleanupScheduler;
import com.notfound.order.domain.model.Order;
import com.notfound.order.domain.model.OrderStatus;
import com.notfound.order.presentation.dto.ApiResponse;
import com.notfound.order.presentation.dto.InternalOrderResponse;
import com.notfound.order.presentation.dto.UpdateOrderStatusRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@Tag(name = "Internal - Order", description = "내부 주문 API (서비스 간 통신)")
@RestController
@RequestMapping("/internal/order")
public class InternalOrderController {

    private final GetInternalOrderUseCase getInternalOrderUseCase;
    private final UpdateOrderStatusUseCase updateOrderStatusUseCase;
    private final ClearCartUseCase clearCartUseCase;
    private final PendingOrderCleanupScheduler pendingOrderCleanupScheduler;

    public InternalOrderController(GetInternalOrderUseCase getInternalOrderUseCase,
                                   UpdateOrderStatusUseCase updateOrderStatusUseCase,
                                   ClearCartUseCase clearCartUseCase,
                                   PendingOrderCleanupScheduler pendingOrderCleanupScheduler) {
        this.getInternalOrderUseCase = getInternalOrderUseCase;
        this.updateOrderStatusUseCase = updateOrderStatusUseCase;
        this.clearCartUseCase = clearCartUseCase;
        this.pendingOrderCleanupScheduler = pendingOrderCleanupScheduler;
    }

    @Operation(summary = "주문 조회", description = "payment-service가 결제 전 주문 정보(총액, 상품 목록) 조회")
    @GetMapping("/{orderId}")
    public ResponseEntity<ApiResponse<InternalOrderResponse>> getOrder(@PathVariable UUID orderId) {
        var detail = getInternalOrderUseCase.getOrder(orderId);
        var response = InternalOrderResponse.from(detail.order(), detail.orderItems());

        return ResponseEntity.ok(
                ApiResponse.success(200, "ORDER_FOUND",
                        "주문 정보를 조회했습니다.", response));
    }

    @Operation(summary = "주문 상태 변경", description = "payment-service가 결제 완료 후 PENDING → PAID 전환")
    @PostMapping("/{orderId}/status")
    public ResponseEntity<ApiResponse<Map<String, Object>>> updateStatus(
            @PathVariable UUID orderId,
            @Valid @RequestBody UpdateOrderStatusRequest request) {

        OrderStatus status = OrderStatus.valueOf(request.status());
        Order order = updateOrderStatusUseCase.updateStatus(orderId, status, request.depositUsed());

        return ResponseEntity.ok(
                ApiResponse.success(200, "ORDER_STATUS_UPDATE_SUCCESS",
                        "주문 상태가 변경되었습니다.",
                        Map.of("orderId", order.getId(),
                                "orderStatus", order.getStatus().name())));
    }

    @Operation(summary = "만료 PENDING 주문 정리", description = "30분 경과한 미결제 주문을 CANCELLED로 수동 전환")
    @PostMapping("/cleanup")
    public ResponseEntity<ApiResponse<Void>> cleanupExpiredOrders() {
        pendingOrderCleanupScheduler.cleanupExpiredPendingOrders();

        return ResponseEntity.ok(
                ApiResponse.success(200, "CLEANUP_SUCCESS",
                        "만료 주문 정리가 완료되었습니다.", null));
    }

    @Operation(summary = "장바구니 전체 삭제", description = "payment-service가 결제 완료 후 해당 회원 장바구니 전체 삭제")
    @DeleteMapping("/cart/{memberId}")
    public ResponseEntity<ApiResponse<Void>> clearCart(@PathVariable UUID memberId) {
        clearCartUseCase.clearCart(memberId);

        return ResponseEntity.ok(
                ApiResponse.success(200, "CART_CLEAR_SUCCESS",
                        "장바구니가 삭제되었습니다.", null));
    }
}
