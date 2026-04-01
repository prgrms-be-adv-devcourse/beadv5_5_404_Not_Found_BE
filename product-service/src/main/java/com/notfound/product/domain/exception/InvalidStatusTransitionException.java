package com.notfound.product.domain.exception;

import com.notfound.product.domain.model.ProductStatus;

public class InvalidStatusTransitionException extends RuntimeException {

    public InvalidStatusTransitionException(ProductStatus current, ProductStatus next) {
        super(current.name() + " → " + next.name() + " 상태 전환은 허용되지 않습니다.");
    }
}
