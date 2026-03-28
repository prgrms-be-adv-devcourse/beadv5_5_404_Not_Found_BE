package com.notfound.product.adapter.in.web.dto;

import com.notfound.product.application.port.in.CreateCategoryCommand;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record CreateCategoryRequest(
        UUID parentId,

        @NotBlank
        @Size(max = 100)
        String name,

        @NotBlank
        @Size(max = 100)
        String slug,

        @Min(0)
        int sortOrder
) {
    public CreateCategoryCommand toCommand() {
        return new CreateCategoryCommand(parentId, name, slug, sortOrder);
    }
}
