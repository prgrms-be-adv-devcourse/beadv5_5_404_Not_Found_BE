package com.notfound.payment.domain.model;

/**
 * 결제 수단 유형.
 * 예치금 전액 결제는 PAYMENT row를 생성하지 않으므로(ERD: ORDER ||--o| PAYMENT)
 * PG 결제 건만 이 enum을 사용한다.
 */
public enum PaymentMethodType {
    PG
}
