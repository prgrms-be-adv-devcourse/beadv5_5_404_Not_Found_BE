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

    public static OrderException orderNotFound() {
        return new OrderException("ORDER_NOT_FOUND", "주문을 찾을 수 없습니다.");
    }

    public static OrderException orderAccessDenied() {
        return new OrderException("ORDER_ACCESS_DENIED", "해당 주문에 접근할 권한이 없습니다.");
    }

    public static OrderException orderCannotBeCancelled() {
        return new OrderException("ORDER_CANCEL_NOT_ALLOWED", "취소할 수 없는 주문 상태입니다.");
    }

    public static OrderException noItemsSelected() {
        return new OrderException("NO_ITEMS_SELECTED", "선택된 상품이 없습니다.");
    }

    public static OrderException emptyOrder() {
        return new OrderException("EMPTY_ORDER", "주문 항목이 비어있습니다.");
    }

    public static OrderException productNotFound() {
        return new OrderException("PRODUCT_NOT_FOUND", "상품을 찾을 수 없습니다.");
    }

    public static OrderException insufficientStock() {
        return new OrderException("INSUFFICIENT_STOCK", "재고가 부족합니다.");
    }

    public static OrderException insufficientDeposit() {
        return new OrderException("INSUFFICIENT_DEPOSIT", "예치금 잔액이 부족합니다.");
    }

    public static OrderException duplicateOrder() {
        return new OrderException("DUPLICATE_ORDER", "중복 주문입니다.");
    }

    public static OrderException addressNotFound() {
        return new OrderException("ADDRESS_NOT_FOUND", "배송지를 찾을 수 없습니다.");
    }

    public static OrderException orderCannotBeConfirmed() {
        return new OrderException("ORDER_CANNOT_BE_CONFIRMED", "배송 완료(DELIVERED) 상태에서만 구매확정이 가능합니다.");
    }

    public static OrderException orderCannotBeReturned() {
        return new OrderException("ORDER_CANNOT_BE_RETURNED", "현재 주문 상태에서는 반품 신청이 불가능합니다.");
    }

    public static OrderException shipmentAccessDenied() {
        return new OrderException("SHIPMENT_ACCESS_DENIED", "배송 정보를 수정할 권한이 없습니다.");
    }

    public static OrderException invalidShipmentStatus() {
        return new OrderException("INVALID_SHIPMENT_STATUS", "유효하지 않은 배송 상태값입니다.");
    }
}
