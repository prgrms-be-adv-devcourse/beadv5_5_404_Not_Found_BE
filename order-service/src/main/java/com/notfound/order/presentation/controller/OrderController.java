package com.notfound.order.presentation.controller;

import com.notfound.order.application.port.in.CheckoutUseCase;
import com.notfound.order.application.port.in.CreateOrderUseCase;
import com.notfound.order.infrastructure.security.AuthUser;
import com.notfound.order.infrastructure.security.AuthenticatedUser;
import com.notfound.order.presentation.dto.ApiResponse;
import com.notfound.order.presentation.dto.CreateOrderRequest;
import com.notfound.order.presentation.dto.OrderResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Tag(name = "Order", description = "주문 API")
@RestController
@RequestMapping("/order")
public class OrderController {

    private final CheckoutUseCase checkoutUseCase;
    private final CreateOrderUseCase createOrderUseCase;

    public OrderController(CheckoutUseCase checkoutUseCase,
                           CreateOrderUseCase createOrderUseCase) {
        this.checkoutUseCase = checkoutUseCase;
        this.createOrderUseCase = createOrderUseCase;
    }

    @Operation(summary = "결제 페이지 정보 조회", description = "상품, 배송지, 예치금 잔액 조회")
    @GetMapping("/checkout")
    public ResponseEntity<ApiResponse<Map<String, Object>>> checkout(
            @AuthUser AuthenticatedUser user,
            @RequestParam(required = false) List<UUID> cartItemIds,
            @RequestParam(required = false) UUID productId,
            @RequestParam(required = false) Integer quantity) {

        UUID memberId = UUID.fromString(user.userId());
        Map<String, Object> result = checkoutUseCase.checkout(memberId, cartItemIds, productId, quantity);

        return ResponseEntity.ok(
                ApiResponse.success(200, "CHECKOUT_READY",
                        "결제 페이지 정보를 조회했습니다.", result));
    }

    @Operation(summary = "주문 생성", description = "주문 생성 + 예치금 결제")
    @PostMapping
    public ResponseEntity<ApiResponse<OrderResponse>> createOrder(
            @AuthUser AuthenticatedUser user,
            @Valid @RequestBody CreateOrderRequest request) {

        UUID memberId = UUID.fromString(user.userId());
        var result = createOrderUseCase.createOrder(memberId, request.toCommand());

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(201, "ORDER_CREATED",
                        "주문이 완료되었습니다.",
                        OrderResponse.from(result.order(), result.orderItems())));
    }
}
