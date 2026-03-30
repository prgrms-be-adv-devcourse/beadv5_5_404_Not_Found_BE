package com.notfound.product.domain.exception;

import java.util.UUID;

public class ProductNotFoundException extends RuntimeException {

    public ProductNotFoundException(UUID productId) {
        super("상품을 찾을 수 없습니다. id=" + productId);
    }
}
