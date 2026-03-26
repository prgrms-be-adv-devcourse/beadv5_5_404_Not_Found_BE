package com.notfound.product.adapter.in.web.dto;

import com.notfound.product.application.port.in.UpdateProductCommand;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record ProductUpdateRequest(

        UUID categoryId,

        @Size(max = 300)
        String title,

        @Size(max = 200)
        String author,

        @Size(max = 100)
        String publisher,

        @Min(0)
        Integer price,

        @Min(0)
        Integer quantity
) {
    public UpdateProductCommand toCommand(UUID productId) {
        return new UpdateProductCommand(productId, categoryId, title, author, publisher, price, quantity);
    }
}
