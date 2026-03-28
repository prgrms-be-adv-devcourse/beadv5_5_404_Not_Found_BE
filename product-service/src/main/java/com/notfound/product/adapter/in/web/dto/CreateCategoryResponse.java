package com.notfound.product.adapter.in.web.dto;

import com.notfound.product.domain.model.Category;

import java.util.UUID;

public record CreateCategoryResponse(
        UUID id,
        UUID parentId,
        String name,
        String slug,
        int depth,
        int sortOrder
) {
    public static CreateCategoryResponse from(Category category) {
        return new CreateCategoryResponse(
                category.getId(),
                category.getParentId(),
                category.getName(),
                category.getSlug(),
                category.getDepth(),
                category.getSortOrder()
        );
    }
}
