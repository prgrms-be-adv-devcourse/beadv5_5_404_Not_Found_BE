package com.notfound.order.application.service;

import com.notfound.order.application.port.out.OrderItemRepository;
import com.notfound.order.application.port.out.OrderRepository;
import com.notfound.order.domain.model.Order;
import com.notfound.order.domain.model.OrderItem;
import com.notfound.order.domain.model.OrderStatus;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class PendingOrderCleanupScheduler {

    private static final Logger log = LoggerFactory.getLogger(PendingOrderCleanupScheduler.class);

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final int expireMinutes;

    public PendingOrderCleanupScheduler(OrderRepository orderRepository,
                                         OrderItemRepository orderItemRepository,
                                         @Value("${order.pending.expire-minutes}") int expireMinutes) {
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.expireMinutes = expireMinutes;
    }

    @Scheduled(fixedRate = 300000)
    @SchedulerLock(name = "pending-order-cleanup", lockAtMostFor = "PT5M")
    @Transactional
    public void cleanupExpiredPendingOrders() {
        LocalDateTime threshold = LocalDateTime.now().minusMinutes(expireMinutes);
        List<Order> expiredOrders = orderRepository.findByStatusAndCreatedBefore(
                OrderStatus.PENDING, threshold);

        if (expiredOrders.isEmpty()) {
            return;
        }

        for (Order order : expiredOrders) {
            order.cancel();
            orderRepository.save(order);

            List<OrderItem> items = orderItemRepository.findByOrderId(order.getId());
            for (OrderItem item : items) {
                item.cancel();
                orderItemRepository.save(item);
            }
        }

        log.info("만료 주문 정리: {}건 CANCELLED 처리", expiredOrders.size());
    }
}
