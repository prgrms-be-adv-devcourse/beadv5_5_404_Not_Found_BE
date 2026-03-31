package com.notfound.payment.presentation;

public record ApiResponse<T>(
        int status,
        String code,
        String message,
        T data
) {
    public static <T> ApiResponse<T> ok(String code, String message, T data) {
        return new ApiResponse<>(200, code, message, data);
    }

    public static <T> ApiResponse<T> of(int status, String code, String message, T data) {
        return new ApiResponse<>(status, code, message, data);
    }

    public static ApiResponse<Void> error(int status, String code, String message) {
        return new ApiResponse<>(status, code, message, null);
    }
}
