package com.notfound.product.adapter.out.rest.dto;

public record MemberApiResponse<T>(
        int status,
        String code,
        String message,
        T data
) {
}
