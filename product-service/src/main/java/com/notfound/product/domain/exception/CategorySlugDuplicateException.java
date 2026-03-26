package com.notfound.product.domain.exception;

public class CategorySlugDuplicateException extends RuntimeException {

    public CategorySlugDuplicateException(String slug) {
        super("이미 사용 중인 슬러그입니다. slug=" + slug);
    }
}
