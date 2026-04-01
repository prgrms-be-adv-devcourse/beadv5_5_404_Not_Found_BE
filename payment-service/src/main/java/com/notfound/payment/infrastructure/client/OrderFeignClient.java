package com.notfound.payment.infrastructure.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;
import java.util.UUID;

@FeignClient(name = "order-service", configuration = InternalFeignConfig.class)
public interface OrderFeignClient {

    @GetMapping("/internal/order/{orderId}")
    ApiResponse<OrderData> getOrder(@PathVariable UUID orderId);

    @PostMapping("/internal/order/{orderId}/status")
    ApiResponse<Void> updateOrderStatus(@PathVariable UUID orderId, @RequestBody OrderStatusRequest request);

    record ApiResponse<T>(int status, String code, String message, T data) {}

    record OrderData(UUID orderId, String status, int totalAmount, List<OrderItemData> items) {}

    record OrderItemData(UUID productId, int quantity) {}

    record OrderStatusRequest(String status, int depositUsed) {}
}
