package com.notfound.order.application.service;

import com.notfound.order.application.port.in.RequestReturnUseCase;
import com.notfound.order.application.port.in.UpdateShipmentUseCase;
import com.notfound.order.application.port.out.OrderItemRepository;
import com.notfound.order.application.port.out.OrderRepository;
import com.notfound.order.application.port.out.ShipmentRepository;
import com.notfound.order.domain.exception.OrderException;
import com.notfound.order.domain.model.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class ShipmentService implements UpdateShipmentUseCase, RequestReturnUseCase {

    private final ShipmentRepository shipmentRepository;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;

    public ShipmentService(ShipmentRepository shipmentRepository,
                           OrderRepository orderRepository,
                           OrderItemRepository orderItemRepository) {
        this.shipmentRepository = shipmentRepository;
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
    }

    @Override
    @Transactional
    public Shipment updateShipment(UUID memberId, UUID orderId, String carrier, String trackingNumber, ShipmentStatus status) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(OrderException::orderNotFound);

        // 판매자 본인 주문의 상품이어야 하지만, 현재는 단순화하여 주문 존재 여부만 확인
        Shipment shipment = shipmentRepository.findByOrderId(orderId)
                .orElseGet(() -> Shipment.builder()
                        .orderId(orderId)
                        .status(ShipmentStatus.PREPARING)
                        .build());

        shipment.update(carrier, trackingNumber, status);

        // 배송 상태에 따라 주문 상태 연동
        if (status == ShipmentStatus.SHIPPED || status == ShipmentStatus.IN_TRANSIT) {
            if (order.getStatus() == OrderStatus.PAID || order.getStatus() == OrderStatus.CONFIRMED) {
                order = Order.builder()
                        .id(order.getId())
                        .orderNumber(order.getOrderNumber())
                        .memberId(order.getMemberId())
                        .status(OrderStatus.SHIPPING)
                        .totalAmount(order.getTotalAmount())
                        .shippingFee(order.getShippingFee())
                        .depositUsed(order.getDepositUsed())
                        .shippingSnapshot(order.getShippingSnapshot())
                        .idempotencyKey(order.getIdempotencyKey())
                        .createdAt(order.getCreatedAt())
                        .deliveredAt(order.getDeliveredAt())
                        .build();
                orderRepository.save(order);
            }
        } else if (status == ShipmentStatus.DELIVERED) {
            order.markDelivered();
            orderRepository.save(order);
        }

        return shipmentRepository.save(shipment);
    }

    @Override
    @Transactional
    public ReturnResult requestReturn(UUID memberId, UUID orderId, String reason, List<UUID> orderItemIds) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(OrderException::orderNotFound);

        if (!order.getMemberId().equals(memberId)) {
            throw OrderException.orderAccessDenied();
        }

        if (order.getStatus() != OrderStatus.DELIVERED) {
            throw OrderException.orderCannotBeReturned();
        }

        // 반품 대상 항목의 상태를 REFUNDED로 변경
        List<OrderItem> items = orderItemRepository.findByOrderId(orderId);
        List<UUID> returnedIds = items.stream()
                .filter(item -> orderItemIds.contains(item.getId()))
                .peek(item -> {
                    item.cancel();
                    orderItemRepository.save(item);
                })
                .map(OrderItem::getId)
                .toList();

        return new ReturnResult(orderId, "PENDING", returnedIds);
    }
}
