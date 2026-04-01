package com.notfound.payment.infrastructure.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;
import java.util.UUID;

@FeignClient(name = "product-service", configuration = InternalFeignConfig.class)
public interface ProductFeignClient {

    @PostMapping("/internal/products/stock/deduct")
    ApiResponse<Void> deductStock(@RequestBody StockDeductRequest request);

    record ApiResponse<T>(int status, String code, String message, T data) {}

    record StockDeductRequest(List<StockItem> items) {}

    record StockItem(UUID productId, int quantity) {}
}
