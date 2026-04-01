package com.notfound.order.application.service;

import com.notfound.order.application.port.out.OrderItemRepository;
import com.notfound.order.application.port.out.OrderRepository;
import com.notfound.order.domain.event.PurchaseConfirmedEvent;
import com.notfound.order.domain.model.Order;
import com.notfound.order.domain.model.OrderItem;
import com.notfound.order.domain.model.OrderStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Component
public class AutoConfirmScheduler {

    private static final Logger log = LoggerFactory.getLogger(AutoConfirmScheduler.class);
    private static final int AUTO_CONFIRM_DAYS = 7;

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final ApplicationEventPublisher eventPublisher;

    public AutoConfirmScheduler(OrderRepository orderRepository,
                                OrderItemRepository orderItemRepository,
                                ApplicationEventPublisher eventPublisher) {
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.eventPublisher = eventPublisher;
    }

    @Scheduled(cron = "0 0 2 * * *")
    @Transactional
    public void autoConfirmDeliveredOrders() {
        LocalDateTime threshold = LocalDateTime.now().minusDays(AUTO_CONFIRM_DAYS);
        List<Order> orders = orderRepository.findByStatusAndDeliveredBefore(
                OrderStatus.DELIVERED, threshold);

        log.info("자동 구매확정 대상: {}건", orders.size());

        for (Order order : orders) {
            try {
                if (order.getStatus() == OrderStatus.PURCHASE_CONFIRMED) {
                    continue;
                }

                order.confirmPurchase();
                orderRepository.save(order);

                List<OrderItem> items = orderItemRepository.findByOrderId(order.getId());
                if (items.isEmpty()) {
                    log.warn("주문 항목 없음, 자동확정 스킵: orderId={}", order.getId());
                    continue;
                }
                UUID sellerId = items.get(0).getSellerId();
                UUID eventId = UUID.nameUUIDFromBytes(("confirm:" + order.getId()).getBytes());
                eventPublisher.publishEvent(new PurchaseConfirmedEvent(
                        eventId, order.getId(), sellerId, order.getTotalAmount(), order.getConfirmedAt()));

                log.info("자동 구매확정 완료: orderId={}", order.getId());
            } catch (Exception e) {
                log.error("자동 구매확정 실패: orderId={}, cause={}", order.getId(), e.getMessage(), e);
            }
        }
    }
}
