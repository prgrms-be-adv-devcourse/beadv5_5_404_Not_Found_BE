package com.notfound.order.infrastructure.client;

import com.notfound.order.application.port.out.ProductServicePort;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * Product 서비스 스텁.
 * Product 모듈 개발 완료 시 RestClient 기반 구현체로 교체 예정.
 */
@Component
public class ProductServiceStub implements ProductServicePort {

    @Override
    public List<Map<String, Object>> getProducts(List<UUID> productIds) {
        return productIds.stream().map(id -> {
            Map<String, Object> product = new LinkedHashMap<>();
            product.put("productId", id.toString());
            product.put("productName", "스텁 도서 - " + id.toString().substring(0, 8));
            product.put("price", 15000);
            product.put("stock", 100);
            product.put("sellerId", UUID.randomUUID().toString());
            product.put("imageUrl", null);
            return product;
        }).toList();
    }
}
