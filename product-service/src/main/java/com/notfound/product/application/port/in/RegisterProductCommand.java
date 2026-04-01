package com.notfound.product.application.port.in;

import com.notfound.product.domain.model.BookType;

import java.util.UUID;

public record RegisterProductCommand(
        UUID sellerId,
        UUID categoryId,
        String isbn,
        String title,
        String author,
        String publisher,
        int price,
        int quantity,
        BookType bookType
) {}
