package com.notfound.product.application.port.out;

import com.notfound.product.domain.model.Category;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CategoryRepository {

    Optional<Category> findById(UUID categoryId);

    List<Category> findAll();

    Category save(Category category);

    boolean existsBySlug(String slug);
}
