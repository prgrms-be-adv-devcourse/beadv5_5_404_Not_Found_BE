package com.notfound.product.adapter.in.web;

import com.notfound.product.adapter.in.web.dto.ApiResponse;
import com.notfound.product.adapter.in.web.dto.ProductErrorCode;
import com.notfound.product.domain.exception.CategoryNotFoundException;
import com.notfound.product.domain.exception.CategorySlugDuplicateException;
import com.notfound.product.domain.exception.ForbiddenException;
import com.notfound.product.domain.exception.InsufficientStockException;
import com.notfound.product.domain.exception.InvalidStatusTransitionException;
import com.notfound.product.domain.exception.IsbnDuplicateException;
import com.notfound.product.domain.exception.ProductNotFoundException;
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
        ProductErrorCode code = resolveErrorCode(e.getMessage());
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ApiResponse.error(code.getStatus(), code.getCode(), e.getMessage()));
    }

    @ExceptionHandler(ProductNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleProductNotFound(ProductNotFoundException e) {
        ProductErrorCode code = ProductErrorCode.PRODUCT_NOT_FOUND;
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error(code.getStatus(), code.getCode(), e.getMessage()));
    }

    @ExceptionHandler(CategoryNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleCategoryNotFound(CategoryNotFoundException e) {
        ProductErrorCode code = ProductErrorCode.CATEGORY_NOT_FOUND;
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error(code.getStatus(), code.getCode(), e.getMessage()));
    }

    @ExceptionHandler(CategorySlugDuplicateException.class)
    public ResponseEntity<ApiResponse<Void>> handleCategorySlugDuplicate(CategorySlugDuplicateException e) {
        ProductErrorCode code = ProductErrorCode.CATEGORY_SLUG_DUPLICATE;
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ApiResponse.error(code.getStatus(), code.getCode(), e.getMessage()));
    }

    @ExceptionHandler(IsbnDuplicateException.class)
    public ResponseEntity<ApiResponse<Void>> handleIsbnDuplicate(IsbnDuplicateException e) {
        ProductErrorCode code = ProductErrorCode.PRODUCT_ISBN_DUPLICATE;
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ApiResponse.error(code.getStatus(), code.getCode(), e.getMessage()));
    }

    @ExceptionHandler(InsufficientStockException.class)
    public ResponseEntity<ApiResponse<Void>> handleInsufficientStock(InsufficientStockException e) {
        ProductErrorCode code = ProductErrorCode.PRODUCT_INSUFFICIENT_STOCK;
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(code.getStatus(), code.getCode(), e.getMessage()));
    }

    @ExceptionHandler(InvalidStatusTransitionException.class)
    public ResponseEntity<ApiResponse<Void>> handleInvalidStatusTransition(InvalidStatusTransitionException e) {
        ProductErrorCode code = ProductErrorCode.INVALID_STATUS_TRANSITION;
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(code.getStatus(), code.getCode(), e.getMessage()));
    }

    private ProductErrorCode resolveErrorCode(String message) {
        if (ProductErrorCode.EMAIL_NOT_VERIFIED.getMessage().equals(message)) {
            return ProductErrorCode.EMAIL_NOT_VERIFIED;
        }
        if (ProductErrorCode.SELLER_NOT_APPROVED.getMessage().equals(message)) {
            return ProductErrorCode.SELLER_NOT_APPROVED;
        }
        return ProductErrorCode.FORBIDDEN;
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Map<String, String>>> handleValidation(
            MethodArgumentNotValidException e) {
        Map<String, String> errors = e.getBindingResult().getFieldErrors().stream()
                .collect(Collectors.toMap(FieldError::getField, FieldError::getDefaultMessage,
                        (existing, replacement) -> existing));
        ProductErrorCode code = ProductErrorCode.INVALID_INPUT_VALUE;
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.success(code.getStatus(), code.getCode(), code.getMessage(), errors));
    }
}
