package com.notfound.order.application.service;

import com.notfound.order.application.port.in.RequestReturnUseCase;
import com.notfound.order.application.port.in.UpdateShipmentUseCase;
import com.notfound.order.application.port.out.OrderItemRepository;
import com.notfound.order.application.port.out.OrderRepository;
import com.notfound.order.application.port.out.ShipmentRepository;
import com.notfound.order.domain.exception.OrderException;
import com.notfound.order.domain.model.Order;
import com.notfound.order.domain.model.OrderItem;
import com.notfound.order.domain.model.OrderStatus;
import com.notfound.order.domain.model.Shipment;
import com.notfound.order.domain.model.ShipmentStatus;
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
    public Shipment updateShipment(UUID memberId, UUID orderId, String carrier, String trackingNumber, ShipmentStatus status, boolean isAdmin) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(OrderException::orderNotFound);

        // ADMIN은 sellerId 검증 바이패스
        if (!isAdmin) {
            boolean isSellerOfOrder = orderItemRepository.findByOrderId(orderId).stream()
                    .anyMatch(item -> memberId.equals(item.getSellerId()));
            if (!isSellerOfOrder) {
                throw OrderException.shipmentAccessDenied();
            }
        }

        Shipment shipment = shipmentRepository.findByOrderId(orderId)
                .orElseGet(() -> Shipment.builder()
                        .orderId(orderId)
                        .status(ShipmentStatus.PREPARING)
                        .build());

        shipment.update(carrier, trackingNumber, status);

        if (status == ShipmentStatus.SHIPPED || status == ShipmentStatus.IN_TRANSIT) {
            if (order.getStatus() == OrderStatus.PAID || order.getStatus() == OrderStatus.CONFIRMED) {
                order.markShipping();
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

        if (orderItemIds == null || orderItemIds.isEmpty()) {
            throw new IllegalArgumentException("반품 대상 항목을 선택해주세요.");
        }

        List<OrderItem> items = orderItemRepository.findByOrderId(orderId);
        List<UUID> returnedIds = items.stream()
                .filter(item -> orderItemIds.contains(item.getId()))
                .peek(item -> {
                    item.cancel();
                    orderItemRepository.save(item);
                })
                .map(OrderItem::getId)
                .toList();

        if (returnedIds.isEmpty()) {
            throw new IllegalArgumentException("유효한 반품 대상 항목이 없습니다.");
        }

        return new ReturnResult(orderId, "PENDING", returnedIds);
    }
}
