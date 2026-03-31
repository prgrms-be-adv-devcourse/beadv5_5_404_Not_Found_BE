package com.notfound.order.domain.exception;

/**
 * 주문 상태 전이 실패 시 던지는 도메인 예외.
 * IllegalStateException과 분리하여 서버 버그가 409로 숨겨지는 것을 방지.
 */
public class InvalidStateTransitionException extends RuntimeException {

    public InvalidStateTransitionException(String message) {
        super(message);
    }
}
