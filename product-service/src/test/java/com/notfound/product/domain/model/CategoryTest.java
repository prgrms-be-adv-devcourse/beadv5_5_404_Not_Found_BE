package com.notfound.product.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class CategoryTest {

    @Test
    @DisplayName("parentId가 null이면 루트 카테고리다")
    void isRoot_whenParentIdIsNull() {
        Category root = Category.of(UUID.randomUUID(), null, "국내도서", "domestic", 0, 1, true);

        assertThat(root.isRoot()).isTrue();
    }

    @Test
    @DisplayName("parentId가 있으면 루트 카테고리가 아니다")
    void isRoot_whenParentIdExists() {
        UUID parentId = UUID.randomUUID();
        Category child = Category.of(UUID.randomUUID(), parentId, "소설", "novel", 1, 1, true);

        assertThat(child.isRoot()).isFalse();
    }
}
