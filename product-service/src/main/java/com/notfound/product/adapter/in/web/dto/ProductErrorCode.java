package com.notfound.product.adapter.in.web.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ProductErrorCode {

    PRODUCT_REGISTER_SUCCESS(HttpStatus.CREATED, "PRODUCT_REGISTER_SUCCESS", "상품이 등록되었습니다."),
    PRODUCT_LIST_GET_SUCCESS(HttpStatus.OK, "PRODUCT_LIST_GET_SUCCESS", "상품 목록 조회에 성공했습니다."),
    PRODUCT_GET_SUCCESS(HttpStatus.OK, "PRODUCT_GET_SUCCESS", "상품 조회에 성공했습니다."),
    PRODUCT_UPDATE_SUCCESS(HttpStatus.OK, "PRODUCT_UPDATE_SUCCESS", "상품이 수정되었습니다."),
    PRODUCT_STATUS_CHANGE_SUCCESS(HttpStatus.OK, "PRODUCT_STATUS_CHANGE_SUCCESS", "상품 상태가 변경되었습니다."),
    STOCK_GET_SUCCESS(HttpStatus.OK, "STOCK_GET_SUCCESS", "재고 조회에 성공했습니다."),
    STOCK_DEDUCT_SUCCESS(HttpStatus.OK, "STOCK_DEDUCT_SUCCESS", "재고가 차감되었습니다."),
    STOCK_RESTORE_SUCCESS(HttpStatus.OK, "STOCK_RESTORE_SUCCESS", "재고가 복구되었습니다."),

    CATEGORY_LIST_GET_SUCCESS(HttpStatus.OK, "CATEGORY_LIST_GET_SUCCESS", "카테고리 목록 조회에 성공했습니다."),
    CATEGORY_CREATE_SUCCESS(HttpStatus.CREATED, "CATEGORY_CREATE_SUCCESS", "카테고리가 등록되었습니다."),

    FORBIDDEN(HttpStatus.FORBIDDEN, "FORBIDDEN", "접근 권한이 없습니다."),
    EMAIL_NOT_VERIFIED(HttpStatus.FORBIDDEN, "EMAIL_NOT_VERIFIED", "이메일 인증이 필요합니다."),
    SELLER_NOT_APPROVED(HttpStatus.FORBIDDEN, "SELLER_NOT_APPROVED", "승인된 판매자가 아닙니다."),

    PRODUCT_NOT_FOUND(HttpStatus.NOT_FOUND, "PRODUCT_NOT_FOUND", "상품을 찾을 수 없습니다."),
    PRODUCT_ISBN_DUPLICATE(HttpStatus.CONFLICT, "PRODUCT_ISBN_DUPLICATE", "이미 등록된 ISBN입니다."),
    CATEGORY_NOT_FOUND(HttpStatus.NOT_FOUND, "CATEGORY_NOT_FOUND", "카테고리를 찾을 수 없습니다."),
    CATEGORY_SLUG_DUPLICATE(HttpStatus.CONFLICT, "CATEGORY_SLUG_DUPLICATE", "이미 사용 중인 슬러그입니다."),
    PRODUCT_INSUFFICIENT_STOCK(HttpStatus.BAD_REQUEST, "PRODUCT_INSUFFICIENT_STOCK", "재고가 부족합니다."),
    PRODUCT_STOCK_CONFLICT(HttpStatus.CONFLICT, "PRODUCT_STOCK_CONFLICT", "재고 처리 중 충돌이 발생했습니다."),
    INVALID_STATUS_TRANSITION(HttpStatus.BAD_REQUEST, "INVALID_STATUS_TRANSITION", "허용되지 않는 상태 전환입니다."),
    INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST, "INVALID_INPUT_VALUE", "입력값이 올바르지 않습니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;

    public int getStatus() { return httpStatus.value(); }
}
