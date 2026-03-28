package com.notfound.product.domain.exception;

import java.util.UUID;

public class InsufficientStockException extends RuntimeException {

    public InsufficientStockException(UUID productId, int requested, int available) {
        super("재고가 부족합니다. productId=" + productId + ", 요청=" + requested + ", 가용=" + available);
    }
}
