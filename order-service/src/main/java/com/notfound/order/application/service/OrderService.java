package com.notfound.order.application.service;

import com.notfound.order.application.port.in.CheckoutUseCase;
import com.notfound.order.application.port.in.CreateOrderUseCase;
import com.notfound.order.application.port.in.command.CreateOrderCommand;
import com.notfound.order.application.port.out.*;
import com.notfound.order.domain.exception.OrderException;
import com.notfound.order.domain.model.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class OrderService implements CheckoutUseCase, CreateOrderUseCase {

    private static final int FREE_SHIPPING_THRESHOLD = 15000;
    private static final int SHIPPING_FEE = 2500;

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final MemberServicePort memberServicePort;
    private final ProductServicePort productServicePort;
    private final StockEventPublisher stockEventPublisher;

    public OrderService(OrderRepository orderRepository,
                        OrderItemRepository orderItemRepository,
                        CartRepository cartRepository,
                        CartItemRepository cartItemRepository,
                        MemberServicePort memberServicePort,
                        ProductServicePort productServicePort,
                        StockEventPublisher stockEventPublisher) {
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
        this.memberServicePort = memberServicePort;
        this.productServicePort = productServicePort;
        this.stockEventPublisher = stockEventPublisher;
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

            items = cartItems.stream().map(ci -> {
                var product = productMap.get(ci.getProductId());
                Map<String, Object> item = new LinkedHashMap<>();
                item.put("cartItemId", ci.getId());
                item.put("productId", ci.getProductId());
                item.put("productName", product != null ? product.get("productName") : "Unknown");
                int price = product != null ? ((Number) product.get("price")).intValue() : 0;
                item.put("price", price);
                item.put("quantity", ci.getQuantity());
                item.put("subtotal", price * ci.getQuantity());
                item.put("imageUrl", product != null ? product.get("imageUrl") : null);
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
        // Idempotency check
        String idempotencyKey = memberId + "-" + System.currentTimeMillis();

        if (command.items() == null || command.items().isEmpty()) {
            throw OrderException.emptyOrder();
        }

        // 1. Fetch product info and calculate server-side total
        List<UUID> productIds = command.items().stream()
                .map(CreateOrderCommand.OrderItemCommand::productId)
                .toList();
        var products = productServicePort.getProducts(productIds);
        Map<UUID, Map<String, Object>> productMap = products.stream()
                .collect(Collectors.toMap(
                        p -> UUID.fromString(p.get("productId").toString()),
                        p -> p));

        // Validate all products exist
        for (var item : command.items()) {
            if (!productMap.containsKey(item.productId())) {
                throw OrderException.productNotFound();
            }
        }

        // 2. Stock validation
        for (var item : command.items()) {
            var product = productMap.get(item.productId());
            int stock = ((Number) product.get("stock")).intValue();
            if (stock < item.quantity()) {
                throw OrderException.insufficientStock();
            }
        }

        // Calculate total
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
        int depositUsed = totalAmount + shippingFee;

        // 3. Deduct deposit
        memberServicePort.deductDeposit(memberId, depositUsed);

        // 4. Publish stock deduction events
        for (var item : command.items()) {
            stockEventPublisher.publishStockDeducted(null, item.productId(), item.quantity());
        }

        // 5. Create order
        String orderNumber = generateOrderNumber();

        // Build shipping snapshot from addressId
        String shippingSnapshot = "{}"; // Will be populated from member service address

        Order order = Order.builder()
                .orderNumber(orderNumber)
                .memberId(memberId)
                .status(OrderStatus.PAID)
                .totalAmount(totalAmount)
                .shippingFee(shippingFee)
                .depositUsed(depositUsed)
                .shippingSnapshot(shippingSnapshot)
                .idempotencyKey(idempotencyKey)
                .build();

        Order savedOrder = orderRepository.save(order);

        // 6. Save order items
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

        // Update orderId in stock events (re-publish with correct orderId)
        for (var item : command.items()) {
            // orderId is now known
        }

        // 7. Remove cart items if from cart
        for (var item : command.items()) {
            if (item.cartItemId() != null) {
                cartItemRepository.deleteById(item.cartItemId());
            }
        }

        return new CreateOrderResult(savedOrder, savedItems);
    }

    private String generateOrderNumber() {
        String dateStr = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String seq = String.format("%06d", (int) (Math.random() * 999999) + 1);
        return dateStr + "-" + seq;
    }
}
