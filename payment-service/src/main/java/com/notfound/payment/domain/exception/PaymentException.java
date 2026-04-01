package com.notfound.payment.domain.exception;

public class PaymentException extends RuntimeException {

    private final String code;

    private PaymentException(String code, String message) {
        super(message);
        this.code = code;
    }

    private PaymentException(String code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    // Payment
    public static PaymentException paymentNotFound() {
        return new PaymentException("PAYMENT_NOT_FOUND", "결제 내역을 찾을 수 없습니다.");
    }

    public static PaymentException paymentAlreadyCompleted() {
        return new PaymentException("PAYMENT_ALREADY_COMPLETED", "이미 완료된 결제입니다.");
    }

    public static PaymentException paymentAlreadyCancelled() {
        return new PaymentException("PAYMENT_ALREADY_CANCELLED", "이미 취소된 결제입니다.");
    }

    public static PaymentException paymentNotCompletable() {
        return new PaymentException("PAYMENT_NOT_COMPLETABLE", "완료 처리할 수 없는 결제 상태입니다.");
    }

    public static PaymentException paymentNotCancellable() {
        return new PaymentException("PAYMENT_NOT_CANCELLABLE", "취소할 수 없는 결제 상태입니다.");
    }

    public static PaymentException duplicateIdempotencyKey() {
        return new PaymentException("DUPLICATE_IDEMPOTENCY_KEY", "중복된 멱등키입니다.");
    }

    // Refund
    public static PaymentException refundNotFound() {
        return new PaymentException("REFUND_NOT_FOUND", "환불 내역을 찾을 수 없습니다.");
    }

    public static PaymentException refundAlreadyCompleted() {
        return new PaymentException("REFUND_ALREADY_COMPLETED", "이미 완료된 환불입니다.");
    }

    public static PaymentException refundAmountExceedsPayment() {
        return new PaymentException("REFUND_AMOUNT_EXCEEDS_PAYMENT", "환불 금액이 결제 금액을 초과합니다.");
    }

    // Deposit
    public static PaymentException depositNotFound() {
        return new PaymentException("DEPOSIT_NOT_FOUND", "예치금 내역을 찾을 수 없습니다.");
    }

    public static PaymentException depositInsufficientBalance() {
        return new PaymentException("DEPOSIT_INSUFFICIENT_BALANCE", "예치금 잔액이 부족합니다.");
    }

    public static PaymentException depositAmountBelowMinimum() {
        return new PaymentException("DEPOSIT_AMOUNT_BELOW_MINIMUM", "최소 충전 금액보다 작습니다.");
    }

    public static PaymentException depositAmountExceedsMaximum() {
        return new PaymentException("DEPOSIT_AMOUNT_EXCEEDS_MAXIMUM", "1회 최대 충전 금액을 초과합니다.");
    }

    public static PaymentException depositBalanceExceedsLimit() {
        return new PaymentException("DEPOSIT_BALANCE_EXCEEDS_LIMIT", "예치금 최대 보유 한도를 초과합니다.");
    }

    // PG
    public static PaymentException pgConfirmFailed() {
        return new PaymentException("PG_CONFIRM_FAILED", "PG 결제 승인에 실패했습니다.");
    }

    public static PaymentException pgConfirmFailed(Throwable cause) {
        return new PaymentException("PG_CONFIRM_FAILED", "PG 결제 승인에 실패했습니다.", cause);
    }

    public static PaymentException pgCancelFailed() {
        return new PaymentException("PG_CANCEL_FAILED", "PG 결제 취소에 실패했습니다.");
    }

    public static PaymentException pgCancelFailed(Throwable cause) {
        return new PaymentException("PG_CANCEL_FAILED", "PG 결제 취소에 실패했습니다.", cause);
    }

    public static PaymentException invalidChargeAmount() {
        return new PaymentException("INVALID_CHARGE_AMOUNT", "충전 금액은 1,000원 이상 500,000원 이하이어야 합니다.");
    }

    public static PaymentException amountMismatch() {
        return new PaymentException("AMOUNT_MISMATCH", "결제 금액이 요청 금액과 일치하지 않습니다.");
    }

    public static PaymentException paymentReadyNotFound() {
        return new PaymentException("PAYMENT_READY_NOT_FOUND", "결제 준비 정보를 찾을 수 없습니다.");
    }

    public static PaymentException paymentAlreadyConfirmed() {
        return new PaymentException("PAYMENT_ALREADY_CONFIRMED", "이미 승인 처리된 결제입니다.");
    }

    public static PaymentException memberNotActive() {
        return new PaymentException("MEMBER_NOT_ACTIVE", "활성 상태의 회원만 예치금 충전이 가능합니다.");
    }

    public static PaymentException emailNotVerified() {
        return new PaymentException("EMAIL_NOT_VERIFIED", "이메일 인증이 완료된 회원만 이용 가능합니다.");
    }

    // Order
    public static PaymentException orderAlreadyPaid(java.util.UUID orderId) {
        return new PaymentException("ORDER_ALREADY_PAID", "이미 결제된 주문입니다: " + orderId);
    }
}
