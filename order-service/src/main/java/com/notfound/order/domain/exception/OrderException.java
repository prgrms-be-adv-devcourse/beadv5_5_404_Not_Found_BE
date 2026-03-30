package com.notfound.order.domain.exception;

public class OrderException extends RuntimeException {

    private final String code;

    public OrderException(String code, String message) {
        super(message);
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    public static OrderException cartNotFound() {
        return new OrderException("CART_NOT_FOUND", "장바구니를 찾을 수 없습니다.");
    }

    public static OrderException cartItemNotFound() {
        return new OrderException("CART_ITEM_NOT_FOUND", "장바구니 항목을 찾을 수 없습니다.");
    }

    public static OrderException cartItemAccessDenied() {
        return new OrderException("CART_ITEM_ACCESS_DENIED", "해당 장바구니 항목에 접근할 권한이 없습니다.");
    }
}
