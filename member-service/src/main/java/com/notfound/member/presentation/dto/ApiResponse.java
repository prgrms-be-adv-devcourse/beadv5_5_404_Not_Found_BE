package com.notfound.member.presentation.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiResponse<T>(
        int status,
        String code,
        String message,
        T data
) {

    public static <T> ApiResponse<T> success(int status, String code, String message, T data) {
        return new ApiResponse<>(status, code, message, data);
    }

    public static <T> ApiResponse<T> error(int status, String code, String message, T data) {
        return new ApiResponse<>(status, code, message, data);
    }

    public static ApiResponse<Void> error(int status, String code, String message) {
        return new ApiResponse<>(status, code, message, null);
    }
}
