package com.notfound.product.adapter.in.web.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.util.List;
import java.util.UUID;

public record DeductStockRequest(
        @NotEmpty List<@Valid StockItem> items
) {

    public record StockItem(
            @NotNull UUID productId,
            @Positive int quantity
    ) {}
}
