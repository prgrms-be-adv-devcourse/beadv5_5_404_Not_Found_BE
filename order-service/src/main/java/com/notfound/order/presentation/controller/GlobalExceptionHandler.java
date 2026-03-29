package com.notfound.order.presentation.controller;

import com.notfound.order.domain.exception.OrderException;
import com.notfound.order.presentation.dto.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(OrderException.class)
    public ResponseEntity<ApiResponse<Void>> handleOrderException(OrderException e) {
        int status = resolveStatus(e.getCode());
        return ResponseEntity.status(status)
                .body(ApiResponse.error(status, e.getCode(), e.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Map<String, String>>> handleValidationException(
            MethodArgumentNotValidException e) {
        Map<String, String> errors = new HashMap<>();
        e.getBindingResult().getFieldErrors()
                .forEach(error -> errors.put(error.getField(), error.getDefaultMessage()));

        return ResponseEntity.badRequest()
                .body(ApiResponse.error(400, "INVALID_INPUT_VALUE", "입력값이 올바르지 않습니다.", errors));
    }

    private int resolveStatus(String code) {
        if (code.contains("NOT_FOUND")) return HttpStatus.NOT_FOUND.value();
        if (code.contains("ACCESS_DENIED")) return HttpStatus.FORBIDDEN.value();
        if (code.contains("UNAUTHORIZED")) return HttpStatus.UNAUTHORIZED.value();
        if (code.contains("CANNOT_BE") || code.contains("INSUFFICIENT_STOCK")) return HttpStatus.CONFLICT.value();
        if (code.contains("INSUFFICIENT_DEPOSIT")) return HttpStatus.BAD_REQUEST.value();
        return HttpStatus.BAD_REQUEST.value();
    }
}
