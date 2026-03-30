package com.notfound.product.domain.exception;

import java.util.UUID;

public class CategoryNotFoundException extends RuntimeException {

    public CategoryNotFoundException(UUID categoryId) {
        super("카테고리를 찾을 수 없습니다. id=" + categoryId);
    }
}
