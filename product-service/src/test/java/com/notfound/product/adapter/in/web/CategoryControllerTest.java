package com.notfound.product.adapter.in.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.notfound.product.adapter.in.web.dto.CreateCategoryRequest;
import com.notfound.product.application.port.in.CreateCategoryUseCase;
import com.notfound.product.application.port.in.GetCategoryListUseCase;
import com.notfound.product.domain.exception.CategoryNotFoundException;
import com.notfound.product.domain.exception.CategorySlugDuplicateException;
import com.notfound.product.domain.model.Category;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class CategoryControllerTest {

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock private GetCategoryListUseCase getCategoryListUseCase;
    @Mock private CreateCategoryUseCase createCategoryUseCase;

    @BeforeEach
    void setUp() {
        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();

        mockMvc = MockMvcBuilders
                .standaloneSetup(new CategoryController(getCategoryListUseCase, createCategoryUseCase))
                .setCustomArgumentResolvers(new HeaderAuthArgumentResolver())
                .setControllerAdvice(new GlobalExceptionHandler())
                .setValidator(validator)
                .build();
    }

    private Category createCategory(UUID id, UUID parentId, int depth) {
        return Category.of(id, parentId, "소설", "novel", depth, 1, true);
    }

    @Nested
    @DisplayName("GET /products/categories - 카테고리 목록 조회")
    class GetCategoryList {

        @Test
        @DisplayName("인증 없이 요청해도 카테고리 목록을 트리 형태로 반환한다")
        void success() throws Exception {
            UUID rootId = UUID.randomUUID();
            UUID childId = UUID.randomUUID();
            List<Category> categories = List.of(
                    createCategory(rootId, null, 0),
                    createCategory(childId, rootId, 1)
            );
            given(getCategoryListUseCase.getCategoryList()).willReturn(categories);

            mockMvc.perform(get("/products/categories"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value("CATEGORY_LIST_GET_SUCCESS"))
                    .andExpect(jsonPath("$.data.length()").value(1))
                    .andExpect(jsonPath("$.data[0].children.length()").value(1));
        }

        @Test
        @DisplayName("카테고리가 없으면 빈 배열을 반환한다")
        void success_emptyList() throws Exception {
            given(getCategoryListUseCase.getCategoryList()).willReturn(List.of());

            mockMvc.perform(get("/products/categories"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value("CATEGORY_LIST_GET_SUCCESS"))
                    .andExpect(jsonPath("$.data.length()").value(0));
        }
    }

    @Nested
    @DisplayName("POST /products/categories - 카테고리 등록")
    class CreateCategory {

        private CreateCategoryRequest validRequest() {
            return new CreateCategoryRequest(null, "국내도서", "domestic", 1);
        }

        @Test
        @DisplayName("ADMIN이 요청하면 201과 등록된 카테고리를 반환한다")
        void success() throws Exception {
            UUID newId = UUID.randomUUID();
            given(createCategoryUseCase.createCategory(any()))
                    .willReturn(createCategory(newId, null, 0));

            mockMvc.perform(post("/products/categories")
                            .header("X-User-Id", UUID.randomUUID().toString())
                            .header("X-Role", "ADMIN")
                            .header("X-Email-Verified", "true")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validRequest())))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.code").value("CATEGORY_CREATE_SUCCESS"))
                    .andExpect(jsonPath("$.data.id").value(newId.toString()));
        }

        @Test
        @DisplayName("인증 헤더가 없으면 403과 FORBIDDEN을 반환한다")
        void fail_whenNoAuth() throws Exception {
            mockMvc.perform(post("/products/categories")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validRequest())))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.code").value("FORBIDDEN"));
        }

        @Test
        @DisplayName("ADMIN이 아니면 403과 FORBIDDEN을 반환한다")
        void fail_whenNotAdmin() throws Exception {
            mockMvc.perform(post("/products/categories")
                            .header("X-User-Id", UUID.randomUUID().toString())
                            .header("X-Role", "SELLER")
                            .header("X-Email-Verified", "true")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validRequest())))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.code").value("FORBIDDEN"));
        }

        @Test
        @DisplayName("name이 빈 값이면 400과 INVALID_INPUT_VALUE를 반환한다")
        void fail_whenNameIsBlank() throws Exception {
            mockMvc.perform(post("/products/categories")
                            .header("X-User-Id", UUID.randomUUID().toString())
                            .header("X-Role", "ADMIN")
                            .header("X-Email-Verified", "true")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(
                                    new CreateCategoryRequest(null, "", "domestic", 1))))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value("INVALID_INPUT_VALUE"))
                    .andExpect(jsonPath("$.data.name").exists());
        }

        @Test
        @DisplayName("slug가 중복이면 409와 CATEGORY_SLUG_DUPLICATE를 반환한다")
        void fail_whenSlugDuplicate() throws Exception {
            given(createCategoryUseCase.createCategory(any()))
                    .willThrow(new CategorySlugDuplicateException("domestic"));

            mockMvc.perform(post("/products/categories")
                            .header("X-User-Id", UUID.randomUUID().toString())
                            .header("X-Role", "ADMIN")
                            .header("X-Email-Verified", "true")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validRequest())))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.code").value("CATEGORY_SLUG_DUPLICATE"));
        }

        @Test
        @DisplayName("존재하지 않는 parentId이면 404와 CATEGORY_NOT_FOUND를 반환한다")
        void fail_whenParentNotFound() throws Exception {
            given(createCategoryUseCase.createCategory(any()))
                    .willThrow(new CategoryNotFoundException(UUID.randomUUID()));

            mockMvc.perform(post("/products/categories")
                            .header("X-User-Id", UUID.randomUUID().toString())
                            .header("X-Role", "ADMIN")
                            .header("X-Email-Verified", "true")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(
                                    new CreateCategoryRequest(UUID.randomUUID(), "소설", "novel", 1))))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.code").value("CATEGORY_NOT_FOUND"));
        }
    }
}
