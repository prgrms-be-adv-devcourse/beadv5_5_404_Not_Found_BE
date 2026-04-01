package com.notfound.order.presentation.controller;

import com.notfound.order.application.port.in.*;
import com.notfound.order.domain.model.OrderStatus;
import com.notfound.order.infrastructure.security.AuthUser;
import com.notfound.order.infrastructure.security.AuthenticatedUser;
import com.notfound.order.presentation.dto.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Tag(name = "Order", description = "주문 API")
@RestController
@RequestMapping("/order")
public class OrderController {

    private final CheckoutUseCase checkoutUseCase;
    private final CreateOrderUseCase createOrderUseCase;
    private final GetOrderListUseCase getOrderListUseCase;
    private final GetOrderDetailUseCase getOrderDetailUseCase;
    private final CancelOrderUseCase cancelOrderUseCase;

    public OrderController(CheckoutUseCase checkoutUseCase,
                           CreateOrderUseCase createOrderUseCase,
                           GetOrderListUseCase getOrderListUseCase,
                           GetOrderDetailUseCase getOrderDetailUseCase,
                           CancelOrderUseCase cancelOrderUseCase) {
        this.checkoutUseCase = checkoutUseCase;
        this.createOrderUseCase = createOrderUseCase;
        this.getOrderListUseCase = getOrderListUseCase;
        this.getOrderDetailUseCase = getOrderDetailUseCase;
        this.cancelOrderUseCase = cancelOrderUseCase;
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
                        "주문이 생성되었습니다.",
                        OrderResponse.from(result.order(), result.orderItems())));
    }

    @Operation(summary = "주문 목록 조회", description = "주문 리스트 (페이징, 상태 필터)")
    @GetMapping
    public ResponseEntity<ApiResponse<Map<String, Object>>> getOrders(
            @AuthUser AuthenticatedUser user,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        UUID memberId = UUID.fromString(user.userId());
        OrderStatus orderStatus = status != null ? OrderStatus.valueOf(status) : null;
        var orders = getOrderListUseCase.getOrders(memberId, orderStatus, PageRequest.of(page, size));

        var content = orders.getContent().stream()
                .map(OrderListResponse::from)
                .toList();

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("content", content);
        result.put("page", orders.getNumber());
        result.put("size", orders.getSize());
        result.put("totalElements", orders.getTotalElements());
        result.put("totalPages", orders.getTotalPages());

        return ResponseEntity.ok(
                ApiResponse.success(200, "ORDER_LIST_FETCH_SUCCESS",
                        "주문 목록 조회에 성공했습니다.", result));
    }

    @Operation(summary = "주문 상세 조회", description = "주문 상세 정보")
    @GetMapping("/{orderId}")
    public ResponseEntity<ApiResponse<OrderResponse>> getOrderDetail(
            @AuthUser AuthenticatedUser user,
            @PathVariable UUID orderId) {

        UUID memberId = UUID.fromString(user.userId());
        var detail = getOrderDetailUseCase.getOrderDetail(memberId, orderId);

        return ResponseEntity.ok(
                ApiResponse.success(200, "ORDER_DETAIL_FETCH_SUCCESS",
                        "주문 상세 조회에 성공했습니다.",
                        OrderResponse.from(detail.order(), detail.orderItems())));
    }

    @Operation(summary = "주문 취소", description = "전체 취소 또는 부분 취소")
    @PostMapping("/{orderId}/cancel")
    public ResponseEntity<ApiResponse<CancelOrderResponse>> cancelOrder(
            @AuthUser AuthenticatedUser user,
            @PathVariable UUID orderId,
            @RequestBody(required = false) CancelOrderRequest request) {

        UUID memberId = UUID.fromString(user.userId());
        List<UUID> orderItemIds = request != null ? request.orderItemIds() : null;

        var result = cancelOrderUseCase.cancelOrder(memberId, orderId, orderItemIds);

        boolean isPartial = orderItemIds != null && !orderItemIds.isEmpty();
        String code = isPartial ? "ORDER_PARTIAL_CANCEL_SUCCESS" : "ORDER_CANCEL_SUCCESS";
        String message = isPartial ? "선택한 항목이 취소되었습니다." : "주문이 취소되었습니다.";

        return ResponseEntity.ok(
                ApiResponse.success(200, code, message,
                        new CancelOrderResponse(
                                result.order().getId(),
                                result.order().getStatus().name(),
                                result.refundAmount(),
                                result.cancelledItemIds(),
                                LocalDateTime.now())));
    }
}
