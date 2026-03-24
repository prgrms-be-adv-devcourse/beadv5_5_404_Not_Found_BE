package com.notfound.product.adapter.in.web.dto;

import jakarta.validation.constraints.Min;

public record StockRequest(
        @Min(1)
        int quantity
) {}
