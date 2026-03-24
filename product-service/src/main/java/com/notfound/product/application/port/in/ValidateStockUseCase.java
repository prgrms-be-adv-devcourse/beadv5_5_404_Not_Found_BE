package com.notfound.product.application.port.in;

public interface ValidateStockUseCase {

    /**
     * 재고가 충분한지 검증한다.
     * 재고가 부족하면 InsufficientStockException을 던진다.
     */
    void validateStock(ValidateStockCommand command);
}
