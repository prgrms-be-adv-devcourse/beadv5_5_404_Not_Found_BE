package com.notfound.product.adapter.in.web.dto;

import com.notfound.product.application.port.in.RegisterProductCommand;
import com.notfound.product.domain.model.BookType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record ProductRegisterRequest(

        @NotNull
        UUID sellerId,

        @NotNull
        UUID categoryId,

        @NotBlank
        @Size(max = 20)
        String isbn,

        @NotBlank
        @Size(max = 300)
        String title,

        @NotBlank
        @Size(max = 200)
        String author,

        @NotBlank
        @Size(max = 100)
        String publisher,

        @Min(0)
        int price,

        @Min(0)
        int quantity,

        @NotNull
        BookType bookType
) {
    public RegisterProductCommand toCommand() {
        return new RegisterProductCommand(
                sellerId, categoryId, isbn, title, author, publisher, price, quantity, bookType
        );
    }
}
