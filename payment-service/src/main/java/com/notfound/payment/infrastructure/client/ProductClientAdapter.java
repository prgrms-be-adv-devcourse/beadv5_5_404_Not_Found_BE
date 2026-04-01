package com.notfound.payment.infrastructure.client;

import com.notfound.payment.application.port.out.ProductPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ProductClientAdapter implements ProductPort {

    private final ProductFeignClient productFeignClient;

    @Override
    public void deductStock(List<StockItem> items) {
        List<ProductFeignClient.StockItem> feignItems = items.stream()
                .map(item -> new ProductFeignClient.StockItem(item.productId(), item.quantity()))
                .toList();
        productFeignClient.deductStock(new ProductFeignClient.StockDeductRequest(feignItems));
    }
}
