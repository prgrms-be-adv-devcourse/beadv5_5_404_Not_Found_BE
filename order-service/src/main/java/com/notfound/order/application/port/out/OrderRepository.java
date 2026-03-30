package com.notfound.order.application.port.out;

import com.notfound.order.domain.model.Order;
import com.notfound.order.domain.model.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface OrderRepository {
    Order save(Order order);
    Optional<Order> findById(UUID orderId);
    Optional<Order> findByIdempotencyKey(String idempotencyKey);
    boolean existsByIdempotencyKey(String idempotencyKey);
    List<Order> findByMemberId(UUID memberId);
    Page<Order> findByMemberIdAndStatus(UUID memberId, OrderStatus status, Pageable pageable);
    Page<Order> findByMemberId(UUID memberId, Pageable pageable);
    List<Order> findByStatusAndDeliveredBefore(OrderStatus status, LocalDateTime before);
}
