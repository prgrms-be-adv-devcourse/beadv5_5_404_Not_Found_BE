package com.notfound.product.adapter.out.persistence;

import com.notfound.product.application.port.out.CategoryRepository;
import com.notfound.product.domain.model.Category;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
public class CategoryRepositoryAdapter implements CategoryRepository {

    private final CategoryJpaRepository categoryJpaRepository;

    public CategoryRepositoryAdapter(CategoryJpaRepository categoryJpaRepository) {
        this.categoryJpaRepository = categoryJpaRepository;
    }

    @Override
    public Optional<Category> findById(UUID categoryId) {
        return categoryJpaRepository.findById(categoryId)
                .map(CategoryJpaEntity::toDomain);
    }
}
