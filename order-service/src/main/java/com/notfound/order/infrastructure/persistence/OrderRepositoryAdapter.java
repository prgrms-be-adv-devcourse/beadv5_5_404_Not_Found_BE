package com.notfound.order.infrastructure.persistence;

import com.notfound.order.application.port.out.OrderRepository;
import com.notfound.order.domain.model.Order;
import com.notfound.order.domain.model.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
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

    @Override
    public Page<Order> findByMemberIdAndStatus(UUID memberId, OrderStatus status, Pageable pageable) {
        return orderJpaRepository.findByMemberIdAndStatus(memberId, status, pageable)
                .map(OrderJpaEntity::toDomain);
    }

    @Override
    public Page<Order> findByMemberId(UUID memberId, Pageable pageable) {
        return orderJpaRepository.findByMemberId(memberId, pageable)
                .map(OrderJpaEntity::toDomain);
    }

    @Override
    public List<Order> findByStatusAndDeliveredBefore(OrderStatus status, LocalDateTime before) {
        return orderJpaRepository.findByStatusAndDeliveredAtBefore(status, before).stream()
                .map(OrderJpaEntity::toDomain)
                .toList();
    }
}
