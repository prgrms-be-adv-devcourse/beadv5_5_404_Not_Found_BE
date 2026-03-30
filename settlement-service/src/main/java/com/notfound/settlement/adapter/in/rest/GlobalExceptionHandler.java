package com.notfound.settlement.adapter.in.rest;

import com.notfound.settlement.adapter.in.rest.dto.ApiResponse;
import com.notfound.settlement.adapter.in.rest.dto.SettlementErrorCode;
import com.notfound.settlement.domain.exception.ForbiddenException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ForbiddenException.class)
    public ResponseEntity<ApiResponse<Void>> handleForbidden(ForbiddenException e) {
        SettlementErrorCode code = resolveErrorCode(e.getMessage());
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ApiResponse.error(code.getStatus(), code.getCode(), e.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Map<String, String>>> handleValidation(
            MethodArgumentNotValidException e) {
        Map<String, String> errors = e.getBindingResult().getFieldErrors().stream()
                .collect(Collectors.toMap(FieldError::getField, FieldError::getDefaultMessage,
                        (existing, replacement) -> existing));
        SettlementErrorCode code = SettlementErrorCode.INVALID_INPUT_VALUE;
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(code.getStatus(), code.getCode(), code.getMessage(), errors));
    }

    private SettlementErrorCode resolveErrorCode(String message) {
        if (SettlementErrorCode.EMAIL_NOT_VERIFIED.getMessage().equals(message)) {
            return SettlementErrorCode.EMAIL_NOT_VERIFIED;
        }
        return SettlementErrorCode.FORBIDDEN;
    }
}
