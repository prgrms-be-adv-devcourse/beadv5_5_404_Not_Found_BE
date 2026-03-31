package com.notfound.order.application.service;

import com.notfound.order.application.port.in.*;
import com.notfound.order.application.port.in.command.CreateOrderCommand;
import com.notfound.order.application.port.out.*;
import com.notfound.order.domain.exception.OrderException;
import com.notfound.order.domain.model.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class OrderService implements CheckoutUseCase, CreateOrderUseCase,
        GetOrderListUseCase, GetOrderDetailUseCase, GetInternalOrderUseCase,
        CancelOrderUseCase, UpdateOrderStatusUseCase {

    private static final int FREE_SHIPPING_THRESHOLD = 15000;
    private static final int SHIPPING_FEE = 2500;

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final MemberServicePort memberServicePort;
    private final ProductServicePort productServicePort;

    public OrderService(OrderRepository orderRepository,
                        OrderItemRepository orderItemRepository,
                        CartRepository cartRepository,
                        CartItemRepository cartItemRepository,
                        MemberServicePort memberServicePort,
                        ProductServicePort productServicePort) {
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
        this.memberServicePort = memberServicePort;
        this.productServicePort = productServicePort;
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> checkout(UUID memberId, List<UUID> cartItemIds, UUID productId, Integer quantity) {
        List<Map<String, Object>> items;

        if (cartItemIds != null && !cartItemIds.isEmpty()) {
            // Cart path: get cart items, then fetch product info
            var cart = cartRepository.findByMemberId(memberId)
                    .orElseThrow(OrderException::cartNotFound);
            var cartItems = cartItemRepository.findByCartId(cart.getId()).stream()
                    .filter(ci -> cartItemIds.contains(ci.getId()))
                    .toList();

            if (cartItems.isEmpty()) {
                throw OrderException.noItemsSelected();
            }

            List<UUID> productIds = cartItems.stream()
                    .map(com.notfound.order.domain.model.CartItem::getProductId)
                    .toList();
            var products = productServicePort.getProducts(productIds);
            Map<UUID, Map<String, Object>> productMap = products.stream()
                    .collect(Collectors.toMap(
                            p -> UUID.fromString(p.get("productId").toString()),
                            p -> p));

            // 상품 정보 누락 검증
            for (var ci : cartItems) {
                if (!productMap.containsKey(ci.getProductId())) {
                    throw OrderException.productNotFound();
                }
            }

            items = cartItems.stream().map(ci -> {
                var product = productMap.get(ci.getProductId());
                int price = ((Number) product.get("price")).intValue();
                Map<String, Object> item = new LinkedHashMap<>();
                item.put("cartItemId", ci.getId());
                item.put("productId", ci.getProductId());
                item.put("productName", product.get("productName"));
                item.put("price", price);
                item.put("quantity", ci.getQuantity());
                item.put("subtotal", price * ci.getQuantity());
                item.put("imageUrl", product.get("imageUrl"));
                return item;
            }).toList();
        } else if (productId != null && quantity != null) {
            // Direct buy path
            var products = productServicePort.getProducts(List.of(productId));
            if (products.isEmpty()) {
                throw OrderException.productNotFound();
            }
            var product = products.get(0);
            int price = ((Number) product.get("price")).intValue();

            Map<String, Object> item = new LinkedHashMap<>();
            item.put("productId", productId);
            item.put("productName", product.get("productName"));
            item.put("price", price);
            item.put("quantity", quantity);
            item.put("subtotal", price * quantity);
            item.put("imageUrl", product.get("imageUrl"));
            items = List.of(item);
        } else {
            throw OrderException.noItemsSelected();
        }

        int totalAmount = items.stream()
                .mapToInt(i -> ((Number) i.get("subtotal")).intValue())
                .sum();
        int shippingFee = totalAmount >= FREE_SHIPPING_THRESHOLD ? 0 : SHIPPING_FEE;

        var addresses = memberServicePort.getAddresses(memberId);
        int depositBalance = memberServicePort.getDepositBalance(memberId);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("items", items);
        result.put("totalAmount", totalAmount);
        result.put("shippingFee", shippingFee);
        result.put("addresses", addresses);
        result.put("depositBalance", depositBalance);
        return result;
    }

    @Override
    @Transactional
    public CreateOrderResult createOrder(UUID memberId, CreateOrderCommand command) {
        // 1. Idempotency check — (memberId + idempotencyKey) 스코프
        String idempotencyKey = command.idempotencyKey();
        Optional<Order> existingOrder = orderRepository.findByIdempotencyKey(idempotencyKey);
        if (existingOrder.isPresent()) {
            Order existing = existingOrder.get();
            if (!existing.getMemberId().equals(memberId)) {
                throw OrderException.orderAccessDenied();
            }
            List<OrderItem> existingItems = orderItemRepository.findByOrderId(existing.getId());
            return new CreateOrderResult(existing, existingItems);
        }

        if (command.items() == null || command.items().isEmpty()) {
            throw OrderException.emptyOrder();
        }

        // 2. 상품 정보 조회 + 서버 금액 계산
        List<UUID> productIds = command.items().stream()
                .map(CreateOrderCommand.OrderItemCommand::productId)
                .toList();
        var products = productServicePort.getProducts(productIds);
        Map<UUID, Map<String, Object>> productMap = products.stream()
                .collect(Collectors.toMap(
                        p -> UUID.fromString(p.get("productId").toString()),
                        p -> p));

        for (var item : command.items()) {
            if (!productMap.containsKey(item.productId())) {
                throw OrderException.productNotFound();
            }
        }

        // 3. 금액 계산 + OrderItem 생성 (상태: PAID — 주문 항목은 결제 완료 기준)
        int totalAmount = 0;
        List<OrderItem> orderItems = new ArrayList<>();
        for (var item : command.items()) {
            var product = productMap.get(item.productId());
            int price = ((Number) product.get("price")).intValue();
            int subtotal = price * item.quantity();
            totalAmount += subtotal;

            orderItems.add(OrderItem.builder()
                    .productId(item.productId())
                    .sellerId(UUID.fromString(product.get("sellerId").toString()))
                    .productTitle((String) product.get("productName"))
                    .unitPrice(price)
                    .quantity(item.quantity())
                    .subtotal(subtotal)
                    .status(OrderItemStatus.PAID)
                    .build());
        }

        int shippingFee = totalAmount >= FREE_SHIPPING_THRESHOLD ? 0 : SHIPPING_FEE;

        // 4. cartItemIds 직렬화 (콤마 구분 UUID 문자열)
        List<UUID> cartIds = command.items().stream()
                .map(CreateOrderCommand.OrderItemCommand::cartItemId)
                .filter(java.util.Objects::nonNull)
                .toList();
        String cartItemIdsStr = Order.serializeCartItemIds(cartIds);

        // 5. 주문 생성 (PENDING, depositUsed=0, snapshot 없음)
        Order order = Order.builder()
                .orderNumber(generateOrderNumber())
                .memberId(memberId)
                .status(OrderStatus.PENDING)
                .totalAmount(totalAmount)
                .shippingFee(shippingFee)
                .depositUsed(0)
                .shippingSnapshot(null)
                .idempotencyKey(idempotencyKey)
                .addressId(command.addressId())
                .cartItemIds(cartItemIdsStr)
                .build();

        Order savedOrder;
        try {
            savedOrder = orderRepository.save(order);
        } catch (org.springframework.dao.DataIntegrityViolationException e) {
            // 동시 요청으로 unique 충돌 시 기존 주문 재조회
            return orderRepository.findByIdempotencyKey(idempotencyKey)
                    .map(existing -> new CreateOrderResult(existing, orderItemRepository.findByOrderId(existing.getId())))
                    .orElseThrow(() -> e);
        }

        // 6. OrderItem 저장
        List<OrderItem> savedItems = new ArrayList<>();
        for (var item : orderItems) {
            savedItems.add(orderItemRepository.save(
                    OrderItem.builder()
                            .orderId(savedOrder.getId())
                            .productId(item.getProductId())
                            .sellerId(item.getSellerId())
                            .productTitle(item.getProductTitle())
                            .unitPrice(item.getUnitPrice())
                            .quantity(item.getQuantity())
                            .subtotal(item.getSubtotal())
                            .status(item.getStatus())
                            .build()));
        }

        // 예치금 차감, 재고 차감, 장바구니 삭제, 배송지 스냅샷은 payment-service 결제 완료 후 처리
        return new CreateOrderResult(savedOrder, savedItems);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Order> getOrders(UUID memberId, OrderStatus status, Pageable pageable) {
        if (status != null) {
            return orderRepository.findByMemberIdAndStatus(memberId, status, pageable);
        }
        return orderRepository.findByMemberId(memberId, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public InternalOrderDetail getOrder(UUID orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(OrderException::orderNotFound);
        List<OrderItem> items = orderItemRepository.findByOrderId(orderId);
        return new InternalOrderDetail(order, items);
    }

    @Override
    @Transactional(readOnly = true)
    public GetOrderDetailUseCase.OrderDetail getOrderDetail(UUID memberId, UUID orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(OrderException::orderNotFound);

        if (!order.getMemberId().equals(memberId)) {
            throw OrderException.orderAccessDenied();
        }

        List<OrderItem> items = orderItemRepository.findByOrderId(orderId);
        return new GetOrderDetailUseCase.OrderDetail(order, items);
    }

    @Override
    @Transactional
    public CancelOrderUseCase.CancelOrderResult cancelOrder(UUID memberId, UUID orderId, List<UUID> orderItemIds) {
        // TODO: 부분취소 재구현 시 이 차단 제거
        if (orderItemIds != null && !orderItemIds.isEmpty()) {
            throw OrderException.partialCancelNotSupported();
        }

        Order order = orderRepository.findById(orderId)
                .orElseThrow(OrderException::orderNotFound);

        if (!order.getMemberId().equals(memberId)) {
            throw OrderException.orderAccessDenied();
        }

        // PENDING, PAID, CONFIRMED에서만 취소 가능
        if (order.getStatus() != OrderStatus.PENDING
                && order.getStatus() != OrderStatus.PAID
                && order.getStatus() != OrderStatus.CONFIRMED) {
            throw OrderException.orderCannotBeCancelled();
        }

        List<OrderItem> items = orderItemRepository.findByOrderId(orderId);
        List<UUID> cancelledIds = items.stream().map(OrderItem::getId).toList();
        int refundAmount;

        if (order.getStatus() == OrderStatus.PENDING) {
            // PENDING 취소: 예치금/재고 처리 없음 — 아직 결제 전
            order.cancel();
            orderRepository.save(order);
            refundAmount = 0;
        } else {
            // PAID/CONFIRMED 취소: 예치금 환급 + 재고 복원
            order.cancel();
            orderRepository.save(order);
            refundAmount = order.getDepositUsed();

            for (OrderItem item : items) {
                item.cancel();
                orderItemRepository.save(item);
                productServicePort.restoreStock(item.getProductId(), item.getQuantity());
            }
            if (refundAmount > 0) {
                memberServicePort.chargeDeposit(memberId, refundAmount);
            }
        }

        Order updatedOrder = orderRepository.findById(orderId).orElse(order);
        return new CancelOrderUseCase.CancelOrderResult(updatedOrder, refundAmount, cancelledIds);
    }

    /**
     * Internal API: payment-service가 결제 완료 후 호출.
     *
     * 멱등 정책:
     * - PENDING → PAID: 정상 처리 (스냅샷 저장 + 장바구니 삭제 + depositUsed 저장)
     * - 이미 PAID → PAID 재요청: 200 반환, 부작용 없음 (payment 재시도 안전)
     * - 그 외 전이: 409 Conflict
     *
     * 동시성: @Version 낙관적 락으로 cancel/pay 경합 방어
     * 트랜잭션: 외부 호출(member address) 실패 시 롤백 — PAID로 잘못 남지 않음
     */
    @Override
    @Transactional
    public Order updateStatus(UUID orderId, OrderStatus status, int depositUsed, LocalDateTime confirmedAt) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(OrderException::orderNotFound);

        if (status == OrderStatus.PAID) {
            // 멱등: 이미 PAID면 외부 호출 없이 바로 반환
            if (order.getStatus() == OrderStatus.PAID) {
                return order;
            }

            // 1. 배송지 스냅샷 조회 (외부 호출 — 실패 시 트랜잭션 롤백)
            String shippingSnapshot = resolveShippingSnapshot(order);

            // 2. 상태 전이
            order.pay(depositUsed, shippingSnapshot);

            // 3. 장바구니 항목 삭제
            order.parseCartItemIds().forEach(cartItemRepository::deleteById);
        } else {
            throw new IllegalStateException("지원하지 않는 상태 전이입니다: " + status);
        }

        if (confirmedAt != null) {
            order.setConfirmedAt(confirmedAt);
        }

        return orderRepository.save(order);
    }

    private String resolveShippingSnapshot(Order order) {
        var addresses = memberServicePort.getAddresses(order.getMemberId());
        return addresses.stream()
                .filter(addr -> order.getAddressId() != null
                        && order.getAddressId().toString().equals(String.valueOf(addr.get("addressId"))))
                .findFirst()
                .map(addr -> {
                    try {
                        Map<String, Object> snapshot = new LinkedHashMap<>();
                        snapshot.put("recipient", addr.get("recipient"));
                        snapshot.put("phone", addr.get("phone"));
                        snapshot.put("zipcode", addr.get("zipcode"));
                        snapshot.put("address1", addr.get("address1"));
                        snapshot.put("address2", addr.get("address2"));
                        return new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(snapshot);
                    } catch (Exception e) {
                        throw new RuntimeException("배송지 스냅샷 직렬화 실패", e);
                    }
                })
                .orElse("{}");
    }

    private String generateOrderNumber() {
        // 날짜 + UUID 앞 8자리로 충돌 방지 (최대 30자)
        String dateStr = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String uniquePart = UUID.randomUUID().toString().replace("-", "").substring(0, 12);
        return dateStr + "-" + uniquePart;
    }
}
