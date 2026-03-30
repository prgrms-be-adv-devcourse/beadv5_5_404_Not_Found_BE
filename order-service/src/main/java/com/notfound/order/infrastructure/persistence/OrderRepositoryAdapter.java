package com.notfound.order.infrastructure.persistence;

import com.notfound.order.application.port.out.OrderRepository;
import com.notfound.order.domain.model.Order;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public class OrderRepositoryAdapter implements OrderRepository {

    private final OrderJpaRepository orderJpaRepository;

    public OrderRepositoryAdapter(OrderJpaRepository orderJpaRepository) {
        this.orderJpaRepository = orderJpaRepository;
    }

    @Override
    public Order save(Order order) {
        return orderJpaRepository.save(OrderJpaEntity.from(order)).toDomain();
    }

    @Override
    public Optional<Order> findById(UUID orderId) {
        return orderJpaRepository.findById(orderId).map(OrderJpaEntity::toDomain);
    }

    @Override
    public Optional<Order> findByIdempotencyKey(String idempotencyKey) {
        return orderJpaRepository.findByIdempotencyKey(idempotencyKey).map(OrderJpaEntity::toDomain);
    }

    @Override
    public boolean existsByIdempotencyKey(String idempotencyKey) {
        return orderJpaRepository.existsByIdempotencyKey(idempotencyKey);
    }
}
