package com.notfound.product.domain.exception;

public class IsbnDuplicateException extends RuntimeException {

    public IsbnDuplicateException(String isbn) {
        super("이미 등록된 ISBN입니다. isbn=" + isbn);
    }
}
