package com.notfound.product.adapter.in.web.dto;

import com.notfound.product.domain.model.Category;

import java.util.List;
import java.util.UUID;

public record CategoryTreeResponse(
        UUID id,
        String name,
        String slug,
        int depth,
        int sortOrder,
        List<CategoryTreeResponse> children
) {
    public static CategoryTreeResponse from(Category category, List<CategoryTreeResponse> children) {
        return new CategoryTreeResponse(
                category.getId(),
                category.getName(),
                category.getSlug(),
                category.getDepth(),
                category.getSortOrder(),
                children
        );
    }
}
