package com.notfound.order.infrastructure.client;

import com.notfound.order.application.port.out.ProductServicePort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class ProductServiceClient implements ProductServicePort {

    private static final Logger log = LoggerFactory.getLogger(ProductServiceClient.class);

    private final RestClient restClient;
    private final RestClient internalRestClient;

    public ProductServiceClient(
            @Value("${service.product.url:http://localhost:8082}") String productServiceUrl,
            @Value("${internal.secret-key}") String internalSecretKey) {
        var factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofSeconds(5));
        factory.setReadTimeout(Duration.ofSeconds(10));

        this.restClient = RestClient.builder()
                .baseUrl(productServiceUrl)
                .requestFactory(factory)
                .build();

        this.internalRestClient = RestClient.builder()
                .baseUrl(productServiceUrl)
                .requestFactory(factory)
                .defaultHeader("X-Internal-Secret", internalSecretKey)
                .build();
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> getProducts(List<UUID> productIds) {
        String ids = productIds.stream()
                .map(UUID::toString)
                .collect(Collectors.joining(","));

        var response = restClient.get()
                .uri("/product?ids={ids}", ids)
                .retrieve()
                .body(new ParameterizedTypeReference<Map<String, Object>>() {});

        if (response == null || response.get("data") == null) return List.of();

        List<Map<String, Object>> products = (List<Map<String, Object>>) response.get("data");
        return products.stream().map(p -> {
            Map<String, Object> mapped = new LinkedHashMap<>();
            mapped.put("productId", String.valueOf(p.get("productId")));
            mapped.put("productName", p.get("title"));
            mapped.put("price", ((Number) p.get("price")).intValue());
            mapped.put("stock", ((Number) p.get("quantity")).intValue());
            mapped.put("sellerId", String.valueOf(p.get("sellerId")));
            mapped.put("imageUrl", null);
            return mapped;
        }).toList();
    }

    @Override
    public void restoreStock(UUID productId, int quantity) {
        // TODO: product-service에 POST /internal/products/stock/restore 엔드포인트 추가 후 교체
        log.warn("[STUB] 재고 복원 요청 — product-service에 restore API 미구현: productId={}, quantity={}",
                productId, quantity);
    }
}
