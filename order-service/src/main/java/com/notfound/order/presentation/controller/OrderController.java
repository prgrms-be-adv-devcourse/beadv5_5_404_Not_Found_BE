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
    private final ConfirmPurchaseUseCase confirmPurchaseUseCase;
    private final UpdateShipmentUseCase updateShipmentUseCase;
    private final RequestReturnUseCase requestReturnUseCase;

    public OrderController(CheckoutUseCase checkoutUseCase,
                           CreateOrderUseCase createOrderUseCase,
                           GetOrderListUseCase getOrderListUseCase,
                           GetOrderDetailUseCase getOrderDetailUseCase,
                           CancelOrderUseCase cancelOrderUseCase,
                           ConfirmPurchaseUseCase confirmPurchaseUseCase,
                           UpdateShipmentUseCase updateShipmentUseCase,
                           RequestReturnUseCase requestReturnUseCase) {
        this.checkoutUseCase = checkoutUseCase;
        this.createOrderUseCase = createOrderUseCase;
        this.getOrderListUseCase = getOrderListUseCase;
        this.getOrderDetailUseCase = getOrderDetailUseCase;
        this.cancelOrderUseCase = cancelOrderUseCase;
        this.confirmPurchaseUseCase = confirmPurchaseUseCase;
        this.updateShipmentUseCase = updateShipmentUseCase;
        this.requestReturnUseCase = requestReturnUseCase;
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

    @Operation(summary = "구매확정", description = "DELIVERED 상태 → PURCHASE_CONFIRMED 전환")
    @PostMapping("/{orderId}/confirm")
    public ResponseEntity<ApiResponse<ConfirmPurchaseResponse>> confirmPurchase(
            @AuthUser AuthenticatedUser user,
            @PathVariable UUID orderId) {

        UUID memberId = UUID.fromString(user.userId());
        var order = confirmPurchaseUseCase.confirmPurchase(memberId, orderId);

        return ResponseEntity.ok(
                ApiResponse.success(200, "PURCHASE_CONFIRM_SUCCESS",
                        "구매확정이 완료되었습니다.",
                        new ConfirmPurchaseResponse(
                                order.getId(),
                                order.getStatus().name(),
                                LocalDateTime.now())));
    }

    @Operation(summary = "반품 신청", description = "DELIVERED 상태에서만 가능")
    @PostMapping("/{orderId}/return")
    public ResponseEntity<ApiResponse<ReturnResponse>> requestReturn(
            @AuthUser AuthenticatedUser user,
            @PathVariable UUID orderId,
            @Valid @RequestBody ReturnRequest request) {

        UUID memberId = UUID.fromString(user.userId());
        var result = requestReturnUseCase.requestReturn(
                memberId, orderId, request.reason(), request.orderItemIds());

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(201, "RETURN_REQUEST_SUCCESS",
                        "반품 신청이 접수되었습니다.",
                        new ReturnResponse(result.orderId(), result.returnStatus(), result.orderItemIds())));
    }

    @Operation(summary = "송장 등록/배송 정보 수정", description = "택배사, 송장번호, 배송 상태 수정")
    @PatchMapping("/{orderId}/shipment")
    public ResponseEntity<ApiResponse<ShipmentResponse>> updateShipment(
            @AuthUser AuthenticatedUser user,
            @PathVariable UUID orderId,
            @Valid @RequestBody UpdateShipmentRequest request) {

        UUID memberId = UUID.fromString(user.userId());
        com.notfound.order.domain.model.ShipmentStatus status = request.shipmentStatus() != null
                ? com.notfound.order.domain.model.ShipmentStatus.valueOf(request.shipmentStatus())
                : null;

        var shipment = updateShipmentUseCase.updateShipment(
                memberId, orderId, request.carrier(), request.trackingNumber(), status);

        return ResponseEntity.ok(
                ApiResponse.success(200, "SHIPMENT_UPDATE_SUCCESS",
                        "배송 정보가 수정되었습니다.", ShipmentResponse.from(shipment)));
    }
}
