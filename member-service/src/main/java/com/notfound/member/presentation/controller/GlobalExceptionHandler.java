package com.notfound.member.presentation.controller;

import com.notfound.member.domain.exception.MemberException;
import com.notfound.member.presentation.dto.ApiResponse;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MemberException.class)
    public ResponseEntity<ApiResponse<Void>> handleMemberException(MemberException e) {
        int status = resolveStatus(e.getCode());
        return ResponseEntity.status(status)
                .body(ApiResponse.error(status, e.getCode(), e.getMessage()));
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiResponse<Void>> handleDataIntegrityViolation(DataIntegrityViolationException e) {
        String message = e.getMostSpecificCause().getMessage();
        if (message != null && message.contains("email")) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(ApiResponse.error(409, "MEMBER_DUPLICATE_EMAIL", "이미 사용 중인 이메일입니다."));
        }
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ApiResponse.error(409, "DATA_INTEGRITY_VIOLATION", "데이터 무결성 제약조건 위반입니다."));
    }

    @ExceptionHandler(MissingRequestHeaderException.class)
    public ResponseEntity<ApiResponse<Void>> handleMissingHeader(MissingRequestHeaderException e) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.error(401, "MEMBER_MISSING_TOKEN", "인증 토큰이 필요합니다."));
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

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<Void>> handleIllegalArgument(IllegalArgumentException e) {
        return ResponseEntity.badRequest()
                .body(ApiResponse.error(400, "INVALID_INPUT_VALUE", e.getMessage()));
    }

    private int resolveStatus(String code) {
        if (code.contains("NOT_FOUND")) return HttpStatus.NOT_FOUND.value();
        if (code.contains("ALREADY_EXISTS") || code.contains("DUPLICATE") || code.contains("ALREADY_WITHDRAWN")) {
            return HttpStatus.CONFLICT.value();
        }
        if (code.contains("FORBIDDEN") || code.contains("EMAIL_NOT_VERIFIED") || code.contains("ACCESS_DENIED")
                || code.contains("NOT_APPROVED") || code.contains("INACTIVE")) {
            return HttpStatus.FORBIDDEN.value();
        }
        // 401 — 인증 관련만 명시 매핑
        if (code.equals("INVALID_CREDENTIALS") || code.equals("INVALID_REFRESH_TOKEN")
                || code.equals("INVALID_PASSWORD") || code.equals("INVALID_ACCESS_TOKEN")
                || code.contains("HIJACKED") || code.contains("UNAUTHORIZED")) {
            return HttpStatus.UNAUTHORIZED.value();
        }
        // 400 — 나머지 (INVALID_SELLER_STATUS, INSUFFICIENT, INVALID_DEPOSIT_AMOUNT 등)
        return HttpStatus.BAD_REQUEST.value();
    }
}
