package com.notfound.order.presentation.controller;

import com.notfound.order.application.port.in.UpdateOrderStatusUseCase;
import com.notfound.order.domain.model.Order;
import com.notfound.order.domain.model.OrderStatus;
import com.notfound.order.presentation.dto.ApiResponse;
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

    private final UpdateOrderStatusUseCase updateOrderStatusUseCase;

    public InternalOrderController(UpdateOrderStatusUseCase updateOrderStatusUseCase) {
        this.updateOrderStatusUseCase = updateOrderStatusUseCase;
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
}
