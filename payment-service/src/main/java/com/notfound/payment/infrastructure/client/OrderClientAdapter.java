package com.notfound.payment.infrastructure.client;

import com.notfound.payment.application.port.out.OrderPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class OrderClientAdapter implements OrderPort {

    private final OrderFeignClient orderFeignClient;

    @Override
    public OrderDetail getOrder(UUID orderId) {
        OrderFeignClient.OrderData data = orderFeignClient.getOrder(orderId).data();
        List<OrderItem> items = data.items().stream()
                .map(item -> new OrderItem(item.productId(), item.quantity()))
                .toList();
        return new OrderDetail(data.orderId(), data.status(), data.totalAmount(), items);
    }

    @Override
    public void updateOrderStatus(UUID orderId, String status, int depositUsed) {
        orderFeignClient.updateOrderStatus(
                orderId,
                new OrderFeignClient.OrderStatusRequest(status, depositUsed)
        );
    }
}
