package com.notfound.settlement.adapter.in.rest.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum SettlementErrorCode {

    SETTLEMENT_LIST_GET_SUCCESS(HttpStatus.OK, "SETTLEMENT_LIST_GET_SUCCESS", "정산 내역 조회에 성공했습니다."),

    FORBIDDEN(HttpStatus.FORBIDDEN, "FORBIDDEN", "접근 권한이 없습니다."),
    EMAIL_NOT_VERIFIED(HttpStatus.FORBIDDEN, "EMAIL_NOT_VERIFIED", "이메일 인증이 필요합니다."),

    INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST, "INVALID_INPUT_VALUE", "입력값이 올바르지 않습니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;

    public int getStatus() {
        return httpStatus.value();
    }
}
