package com.notfound.product.application.service;

import com.notfound.product.application.port.in.CreateCategoryCommand;
import com.notfound.product.application.port.out.CategoryRepository;
import com.notfound.product.domain.exception.CategoryNotFoundException;
import com.notfound.product.domain.exception.CategorySlugDuplicateException;
import com.notfound.product.domain.model.Category;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private CategoryService categoryService;

    private UUID categoryId;

    @BeforeEach
    void setUp() {
        categoryId = UUID.randomUUID();
    }

    private Category createCategory(UUID id, UUID parentId, int depth) {
        return Category.of(id, parentId, "소설", "novel", depth, 1, true);
    }

    @Nested
    @DisplayName("카테고리 목록 조회")
    class GetCategoryList {

        @Test
        @DisplayName("전체 카테고리 목록을 반환한다")
        void success_getCategoryList() {
            List<Category> categories = List.of(
                    createCategory(UUID.randomUUID(), null, 0),
                    createCategory(UUID.randomUUID(), null, 0)
            );
            given(categoryRepository.findAll()).willReturn(categories);

            List<Category> result = categoryService.getCategoryList();

            assertThat(result).hasSize(2);
            verify(categoryRepository).findAll();
        }
    }

    @Nested
    @DisplayName("카테고리 등록")
    class CreateCategory {

        @Test
        @DisplayName("parentId가 null이면 depth 0인 루트 카테고리가 등록된다")
        void success_createRootCategory() {
            CreateCategoryCommand command = new CreateCategoryCommand(null, "국내도서", "domestic", 1);
            given(categoryRepository.existsBySlug("domestic")).willReturn(false);
            given(categoryRepository.save(any(Category.class)))
                    .willAnswer(invocation -> invocation.getArgument(0));

            Category result = categoryService.createCategory(command);

            assertThat(result.getDepth()).isEqualTo(0);
            assertThat(result.isRoot()).isTrue();
            verify(categoryRepository).save(any(Category.class));
        }

        @Test
        @DisplayName("parentId가 있으면 부모 depth + 1로 자식 카테고리가 등록된다")
        void success_createChildCategory() {
            UUID parentId = UUID.randomUUID();
            Category parent = createCategory(parentId, null, 0);
            CreateCategoryCommand command = new CreateCategoryCommand(parentId, "소설", "novel", 1);

            given(categoryRepository.existsBySlug("novel")).willReturn(false);
            given(categoryRepository.findById(parentId)).willReturn(Optional.of(parent));
            given(categoryRepository.save(any(Category.class)))
                    .willAnswer(invocation -> invocation.getArgument(0));

            Category result = categoryService.createCategory(command);

            assertThat(result.getDepth()).isEqualTo(1);
            assertThat(result.getParentId()).isEqualTo(parentId);
        }

        @Test
        @DisplayName("slug가 중복이면 CategorySlugDuplicateException이 발생한다")
        void fail_whenSlugDuplicate() {
            CreateCategoryCommand command = new CreateCategoryCommand(null, "국내도서", "domestic", 1);
            given(categoryRepository.existsBySlug("domestic")).willReturn(true);

            assertThatThrownBy(() -> categoryService.createCategory(command))
                    .isInstanceOf(CategorySlugDuplicateException.class);
        }

        @Test
        @DisplayName("존재하지 않는 parentId이면 CategoryNotFoundException이 발생한다")
        void fail_whenParentNotFound() {
            UUID parentId = UUID.randomUUID();
            CreateCategoryCommand command = new CreateCategoryCommand(parentId, "소설", "novel", 1);

            given(categoryRepository.existsBySlug("novel")).willReturn(false);
            given(categoryRepository.findById(parentId)).willReturn(Optional.empty());

            assertThatThrownBy(() -> categoryService.createCategory(command))
                    .isInstanceOf(CategoryNotFoundException.class);
        }
    }
}
