package com.notfound.product.adapter.in.web.dto;

public record ApiResponse<T>(int status, String code, String message, T data) {

    public static <T> ApiResponse<T> success(int status, String code, String message, T data) {
        return new ApiResponse<>(status, code, message, data);
    }

    public static <T> ApiResponse<T> error(int status, String code, String message) {
        return new ApiResponse<>(status, code, message, null);
    }
}
