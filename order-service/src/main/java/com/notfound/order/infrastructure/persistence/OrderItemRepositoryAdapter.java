package com.notfound.order.infrastructure.persistence;

import com.notfound.order.application.port.out.OrderItemRepository;
import com.notfound.order.domain.model.OrderItem;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public class OrderItemRepositoryAdapter implements OrderItemRepository {

    private final OrderItemJpaRepository orderItemJpaRepository;

    public OrderItemRepositoryAdapter(OrderItemJpaRepository orderItemJpaRepository) {
        this.orderItemJpaRepository = orderItemJpaRepository;
    }

    @Override
    public OrderItem save(OrderItem orderItem) {
        return orderItemJpaRepository.save(OrderItemJpaEntity.from(orderItem)).toDomain();
    }

    @Override
    public List<OrderItem> saveAll(List<OrderItem> orderItems) {
        var entities = orderItems.stream()
                .map(OrderItemJpaEntity::from)
                .toList();
        return orderItemJpaRepository.saveAll(entities).stream()
                .map(OrderItemJpaEntity::toDomain)
                .toList();
    }

    @Override
    public List<OrderItem> findByOrderId(UUID orderId) {
        return orderItemJpaRepository.findByOrderId(orderId).stream()
                .map(OrderItemJpaEntity::toDomain)
                .toList();
    }
}
