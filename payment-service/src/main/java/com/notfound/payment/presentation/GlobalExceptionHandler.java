package com.notfound.payment.presentation;

import com.notfound.payment.domain.exception.PaymentException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(PaymentException.class)
    public ResponseEntity<ApiResponse<Void>> handlePaymentException(PaymentException e) {
        HttpStatus status = resolveStatus(e.getCode());
        log.warn("PaymentException: code={}, message={}", e.getCode(), e.getMessage());
        return ResponseEntity.status(status)
                .body(ApiResponse.error(status.value(), e.getCode(), e.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Map<String, String>>> handleValidationException(MethodArgumentNotValidException e) {
        Map<String, String> errors = e.getBindingResult().getFieldErrors().stream()
                .collect(Collectors.toMap(FieldError::getField, FieldError::getDefaultMessage, (a, b) -> a));
        return ResponseEntity.badRequest()
                .body(ApiResponse.of(400, "INVALID_INPUT_VALUE", "입력값이 올바르지 않습니다.", errors));
    }

    @ExceptionHandler({
            MissingRequestHeaderException.class,
            MissingServletRequestParameterException.class,
            MethodArgumentTypeMismatchException.class,
            HttpMessageNotReadableException.class
    })
    public ResponseEntity<ApiResponse<Void>> handleBadRequestException(Exception e) {
        log.warn("잘못된 요청: {}", e.getMessage());
        return ResponseEntity.badRequest()
                .body(ApiResponse.error(400, "BAD_REQUEST", "잘못된 요청입니다."));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGenericException(Exception e) {
        log.error("처리되지 않은 예외 발생", e);
        return ResponseEntity.internalServerError()
                .body(ApiResponse.error(500, "INTERNAL_SERVER_ERROR", "요청을 처리하는 도중 서버에서 문제가 발생했습니다."));
    }

    private HttpStatus resolveStatus(String code) {
        return switch (code) {
            case "PAYMENT_NOT_FOUND", "DEPOSIT_NOT_FOUND", "REFUND_NOT_FOUND",
                 "SETTLEMENT_NOT_FOUND", "PAYMENT_READY_NOT_FOUND" -> HttpStatus.NOT_FOUND;
            case "PAYMENT_ALREADY_COMPLETED", "PAYMENT_ALREADY_CONFIRMED",
                 "REFUND_ALREADY_COMPLETED", "SETTLEMENT_ALREADY_COMPLETED",
                 "ORDER_ALREADY_PAID" -> HttpStatus.CONFLICT;
            case "PG_CONFIRM_FAILED", "PG_CANCEL_FAILED" -> HttpStatus.BAD_GATEWAY;
            case "EMAIL_NOT_VERIFIED" -> HttpStatus.FORBIDDEN;
            default -> HttpStatus.BAD_REQUEST;
        };
    }
}
