package com.notfound.product.application.service;

import com.notfound.product.application.port.in.CreateCategoryCommand;
import com.notfound.product.application.port.in.CreateCategoryUseCase;
import com.notfound.product.application.port.in.GetCategoryListUseCase;
import com.notfound.product.application.port.out.CategoryRepository;
import com.notfound.product.domain.exception.CategoryNotFoundException;
import com.notfound.product.domain.exception.CategorySlugDuplicateException;
import com.notfound.product.domain.model.Category;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CategoryService implements GetCategoryListUseCase, CreateCategoryUseCase {

    private final CategoryRepository categoryRepository;

    @Transactional(readOnly = true)
    @Override
    public List<Category> getCategoryList() {
        return categoryRepository.findAll();
    }

    @Transactional
    @Override
    public Category createCategory(CreateCategoryCommand command) {
        if (categoryRepository.existsBySlug(command.slug())) {
            throw new CategorySlugDuplicateException(command.slug());
        }

        int depth = 0;
        if (command.parentId() != null) {
            Category parent = categoryRepository.findById(command.parentId())
                    .orElseThrow(() -> new CategoryNotFoundException(command.parentId()));
            depth = parent.getDepth() + 1;
        }

        Category category = Category.of(
                UUID.randomUUID(),
                command.parentId(),
                command.name(),
                command.slug(),
                depth,
                command.sortOrder(),
                true
        );

        return categoryRepository.save(category);
    }
}
