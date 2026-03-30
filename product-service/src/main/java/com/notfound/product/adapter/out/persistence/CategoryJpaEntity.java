package com.notfound.product.adapter.out.persistence;

import com.notfound.product.domain.model.Category;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Table(name = "category")
@Getter
@NoArgsConstructor
public class CategoryJpaEntity {

    @Id
    private UUID id;

    @Column(name = "parent_id")
    private UUID parentId;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(unique = true, nullable = false, length = 100)
    private String slug;

    @Column(nullable = false)
    private int depth;

    @Column(name = "sort_order", nullable = false)
    private int sortOrder;

    @Column(name = "is_active", nullable = false)
    private boolean isActive;

    public static CategoryJpaEntity from(Category category) {
        CategoryJpaEntity entity = new CategoryJpaEntity();
        entity.id = category.getId();
        entity.parentId = category.getParentId();
        entity.name = category.getName();
        entity.slug = category.getSlug();
        entity.depth = category.getDepth();
        entity.sortOrder = category.getSortOrder();
        entity.isActive = category.isActive();
        return entity;
    }

    public Category toDomain() {
        return Category.of(id, parentId, name, slug, depth, sortOrder, isActive);
    }
}
