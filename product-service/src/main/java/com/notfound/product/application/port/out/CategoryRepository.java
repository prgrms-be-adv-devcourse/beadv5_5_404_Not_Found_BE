package com.notfound.product.application.port.out;

import com.notfound.product.domain.model.Category;

import java.util.Optional;
import java.util.UUID;

public interface CategoryRepository {

    Optional<Category> findById(UUID categoryId);
}
