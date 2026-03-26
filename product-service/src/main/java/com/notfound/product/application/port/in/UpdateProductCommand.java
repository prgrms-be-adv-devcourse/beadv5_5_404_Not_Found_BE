package com.notfound.product.application.port.in;

import java.util.UUID;

public record UpdateProductCommand(
        UUID sellerId,
        UUID productId,
        UUID categoryId,
        String title,
        String author,
        String publisher,
        Integer price,
        Integer quantity
) {}
