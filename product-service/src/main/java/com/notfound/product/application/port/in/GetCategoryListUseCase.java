package com.notfound.product.application.port.in;

import com.notfound.product.domain.model.Category;

import java.util.List;

public interface GetCategoryListUseCase {

    List<Category> getCategoryList();
}
