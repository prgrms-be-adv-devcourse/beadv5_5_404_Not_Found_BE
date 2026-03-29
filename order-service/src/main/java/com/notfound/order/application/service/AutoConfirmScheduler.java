package com.notfound.order.application.service;

import com.notfound.order.application.port.out.OrderRepository;
import com.notfound.order.application.port.out.PurchaseEventPublisher;
import com.notfound.order.domain.model.Order;
import com.notfound.order.domain.model.OrderStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class AutoConfirmScheduler {

    private static final Logger log = LoggerFactory.getLogger(AutoConfirmScheduler.class);
    private static final int AUTO_CONFIRM_DAYS = 7;

    private final OrderRepository orderRepository;
    private final PurchaseEventPublisher purchaseEventPublisher;

    public AutoConfirmScheduler(OrderRepository orderRepository,
                                PurchaseEventPublisher purchaseEventPublisher) {
        this.orderRepository = orderRepository;
        this.purchaseEventPublisher = purchaseEventPublisher;
    }

    @Scheduled(cron = "0 0 2 * * *")
    @Transactional
    public void autoConfirmDeliveredOrders() {
        LocalDateTime threshold = LocalDateTime.now().minusDays(AUTO_CONFIRM_DAYS);
        List<Order> orders = orderRepository.findByStatusAndDeliveredBefore(
                OrderStatus.DELIVERED, threshold);

        log.info("자동 구매확정 대상: {}건", orders.size());

        for (Order order : orders) {
            order.confirmPurchase();
            orderRepository.save(order);

            purchaseEventPublisher.publishPurchaseConfirmed(
                    order.getId(), order.getMemberId(), order.getTotalAmount());

            log.info("자동 구매확정 완료: orderId={}", order.getId());
        }
    }
}
