package com.notfound.product.adapter.in.web.dto;

import com.notfound.product.domain.model.Product;
import com.notfound.product.domain.model.ProductStatus;

import java.util.UUID;

public record StockResponse(
        UUID productId,
        int quantity,
        ProductStatus status
) {
    public static StockResponse from(Product product) {
        return new StockResponse(product.getId(), product.getQuantity(), product.getStatus());
    }
}
