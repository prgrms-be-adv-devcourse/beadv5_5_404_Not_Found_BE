package com.notfound.product.application.port.in;

import com.notfound.product.domain.model.Category;

public interface CreateCategoryUseCase {

    Category createCategory(CreateCategoryCommand command);
}
