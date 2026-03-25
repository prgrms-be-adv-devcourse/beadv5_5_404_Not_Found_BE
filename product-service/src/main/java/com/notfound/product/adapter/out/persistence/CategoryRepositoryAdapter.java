package com.notfound.product.adapter.out.persistence;

import com.notfound.product.application.port.out.CategoryRepository;
import com.notfound.product.domain.model.Category;
import org.springframework.stereotype.Component;

import java.util.List;
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

    @Override
    public List<Category> findAll() {
        return categoryJpaRepository.findAll().stream()
                .map(CategoryJpaEntity::toDomain)
                .toList();
    }

    @Override
    public Category save(Category category) {
        return categoryJpaRepository.save(CategoryJpaEntity.from(category)).toDomain();
    }

    @Override
    public boolean existsBySlug(String slug) {
        return categoryJpaRepository.existsBySlug(slug);
    }
}
