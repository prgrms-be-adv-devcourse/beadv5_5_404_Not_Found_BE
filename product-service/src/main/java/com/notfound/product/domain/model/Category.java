package com.notfound.product.domain.model;

import lombok.Getter;

import java.util.UUID;

@Getter
public class Category {

    private final UUID id;
    private final UUID parentId;
    private final String name;
    private final String slug;
    private final int depth;
    private final int sortOrder;
    private final boolean isActive;

    private Category(UUID id, UUID parentId, String name, String slug, int depth, int sortOrder, boolean isActive) {
        this.id = id;
        this.parentId = parentId;
        this.name = name;
        this.slug = slug;
        this.depth = depth;
        this.sortOrder = sortOrder;
        this.isActive = isActive;
    }

    public static Category of(UUID id, UUID parentId, String name, String slug, int depth, int sortOrder, boolean isActive) {
        return new Category(id, parentId, name, slug, depth, sortOrder, isActive);
    }

    public boolean isRoot() {
        return parentId == null;
    }

}
